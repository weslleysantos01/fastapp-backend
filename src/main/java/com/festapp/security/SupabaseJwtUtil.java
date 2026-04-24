package com.festapp.security;

import io.jsonwebtoken.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECPublicKeySpec;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class SupabaseJwtUtil {

    @Value("${supabase.url}")
    private String supabaseUrl;

    // Thread-safe com ReadWriteLock — leituras paralelas, escrita exclusiva
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile PublicKey publicKey;
    private volatile long publicKeyCarregadaEm = 0;
    private static final long CACHE_EXPIRACAO_MS = 3600_000; // 1 hora

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private PublicKey getPublicKey() throws Exception {
        long agora = System.currentTimeMillis();

        // Tenta ler do cache sem bloquear
        lock.readLock().lock();
        try {
            if (publicKey != null && (agora - publicKeyCarregadaEm) < CACHE_EXPIRACAO_MS) {
                return publicKey;
            }
        } finally {
            lock.readLock().unlock();
        }

        // Cache expirou ou é nulo — precisa renovar com lock de escrita
        lock.writeLock().lock();
        try {
            // Double-check: outro thread pode ter renovado enquanto esperávamos
            agora = System.currentTimeMillis();
            if (publicKey != null && (agora - publicKeyCarregadaEm) < CACHE_EXPIRACAO_MS) {
                return publicKey;
            }

            publicKey = carregarChavePublica();
            publicKeyCarregadaEm = System.currentTimeMillis();
            return publicKey;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private PublicKey carregarChavePublica() throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(supabaseUrl + "/auth/v1/.well-known/jwks.json");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false); // Evita redirect injection

            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("JWKS endpoint retornou status: " + statusCode);
            }

            StringBuilder sb = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                while (scanner.hasNext()) sb.append(scanner.nextLine());
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> jwks = mapper.readValue(sb.toString(), Map.class);
            List<Map<String, Object>> keys =
                    (List<Map<String, Object>>) jwks.get("keys");

            if (keys == null || keys.isEmpty()) {
                throw new RuntimeException("Nenhuma chave encontrada no JWKS");
            }

            // Pega a primeira chave EC P-256 disponível
            Map<String, Object> key = keys.stream()
                    .filter(k -> "EC".equals(k.get("kty")) && "P-256".equals(k.get("crv")))
                    .findFirst()
                    .orElse(keys.get(0));

            byte[] xBytes = Base64.getUrlDecoder().decode((String) key.get("x"));
            byte[] yBytes = Base64.getUrlDecoder().decode((String) key.get("y"));

            BigInteger x = new BigInteger(1, xBytes);
            BigInteger y = new BigInteger(1, yBytes);

            org.bouncycastle.jce.spec.ECNamedCurveParameterSpec spec =
                    org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec("P-256");
            org.bouncycastle.jce.spec.ECNamedCurveSpec params =
                    new org.bouncycastle.jce.spec.ECNamedCurveSpec(
                            "P-256", spec.getCurve(), spec.getG(), spec.getN());

            java.security.spec.ECPoint point = new java.security.spec.ECPoint(x, y);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);

            return KeyFactory.getInstance("EC", "BC").generatePublic(pubKeySpec);

        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public Claims extrairClaims(String token) {
        try {
            // Deixa o jjwt verificar expiração internamente — mais confiável
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .setAllowedClockSkewSeconds(30) // 30s é suficiente.
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expirado");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new JwtException("Token malformado");
        } catch (Exception e) {
            throw new JwtException("Token inválido");
        }
    }

    public String extrairEmail(String token) {
        String email = extrairClaims(token).get("email", String.class);
        if (email == null || email.isBlank()) {
            throw new JwtException("Token sem email");
        }
        return email;
    }

    public String extrairPerfil(String token) {
        Claims claims = extrairClaims(token);
        Map<String, Object> appMeta = (Map<String, Object>) claims.get("app_metadata");
        if (appMeta == null) return "FUNCIONARIO";
        Object perfil = appMeta.get("perfil");
        if (!(perfil instanceof String)) return "FUNCIONARIO";
        return (String) perfil;
    }

    public Long extrairEmpresaId(String token) {
        Claims claims = extrairClaims(token);
        Map<String, Object> appMeta = (Map<String, Object>) claims.get("app_metadata");
        if (appMeta == null) return null;
        Object id = appMeta.get("empresa_id");
        if (id == null) return null;
        try {
            return Long.valueOf(id.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean validarToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            extrairClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}