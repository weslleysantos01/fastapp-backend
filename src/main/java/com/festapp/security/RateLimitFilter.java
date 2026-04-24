package com.festapp.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Instant> ultimoUso = new ConcurrentHashMap<>();
    private volatile Instant ultimaLimpeza = Instant.now();

    // IPs de proxies confiáveis (ex: Nginx local). Vazio = ignora X-Forwarded-For
    @Value("#{T(java.util.Set).of('${rate.limit.trusted-proxies:}')}")
    private Set<String> trustedProxies;

    private static final int MAX_BUCKETS = 10_000;
    private static final int LIMITE_REQUISICOES = 30;
    private static final Duration JANELA_TEMPO = Duration.ofMinutes(1);
    private static final Duration EXPIRACAO_BUCKET = Duration.ofMinutes(10);
    private static final Duration INTERVALO_LIMPEZA = Duration.ofMinutes(5);

    private Bucket criarBucket() {
        Bandwidth limite = Bandwidth.classic(
                LIMITE_REQUISICOES,
                Refill.greedy(LIMITE_REQUISICOES, JANELA_TEMPO)
        );
        return Bucket.builder().addLimit(limite).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // Só confia no X-Forwarded-For se vier de um proxy configurado como confiável
        if (!trustedProxies.isEmpty() && trustedProxies.contains(remoteAddr)) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isBlank()) {
                String ip = xfHeader.split(",")[0].trim();
                // Valida IPv4 ou IPv6
                if (ip.matches("^\\d{1,3}(\\.\\d{1,3}){3}$") || ip.matches("^[0-9a-fA-F:]+$")) {
                    return ip;
                }
            }
        }

        // IP real da conexão TCP — não pode ser falsificado pelo cliente
        return remoteAddr;
    }

    private void limparBucketsAntigos() {
        Instant agora = Instant.now();
        if (Duration.between(ultimaLimpeza, agora).compareTo(INTERVALO_LIMPEZA) < 0) return;

        ultimaLimpeza = agora;
        ultimoUso.entrySet().removeIf(entry -> {
            boolean expirado = Duration.between(entry.getValue(), agora).compareTo(EXPIRACAO_BUCKET) > 0;
            if (expirado) buckets.remove(entry.getKey());
            return expirado;
        });
    }

    private void forcarLimpezaSeBucketsCheios() {
        if (buckets.size() < MAX_BUCKETS) return;

        Instant agora = Instant.now();
        ultimaLimpeza = agora.minus(INTERVALO_LIMPEZA); // força próxima limpeza rodar
        limparBucketsAntigos();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Rotas isentas de rate limit
        String uri = request.getRequestURI();
        if (uri.equals("/stripe/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }

        limparBucketsAntigos();
        forcarLimpezaSeBucketsCheios();

        String ip = getClientIP(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> criarBucket());
        ultimoUso.put(ip, Instant.now());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"erro\":\"Muitas requisições. Tente novamente em 1 minuto.\"}");
        }
    }
}