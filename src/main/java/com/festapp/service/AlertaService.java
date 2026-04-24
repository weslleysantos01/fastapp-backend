package com.festapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaService.class);

    @Value("${resend.api.key}")
    private String resendApiKey;

    // Fix: RestTemplate injetado como bean — não instanciado a cada uso
    @Autowired
    private RestTemplate restTemplate;

    // Sanitização contra HTML injection
    private String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public void enviarAlertaAtraso(String emailDona, String nomeFuncionario,
                                   String nomeCliente, String dataHora, String endereco) {

        nomeFuncionario = safe(nomeFuncionario);
        nomeCliente     = safe(nomeCliente);
        dataHora        = safe(dataHora);
        endereco        = safe(endereco);

        String html = "<div style='font-family: sans-serif; max-width: 600px;'>"
                + "<h2 style='color: #b91c1c;'>⚠️ Alerta de Atraso — FestApp</h2>"
                + "<p>O funcionário <strong>" + nomeFuncionario + "</strong> ainda não registrou chegada.</p>"
                + "<table style='width:100%; border-collapse:collapse; margin:16px 0;'>"
                + "<tr><td style='padding:8px; background:#f3f4f6; font-weight:bold;'>Cliente</td>"
                + "<td style='padding:8px;'>" + nomeCliente + "</td></tr>"
                + "<tr><td style='padding:8px; background:#f3f4f6; font-weight:bold;'>Data e Hora</td>"
                + "<td style='padding:8px;'>" + dataHora + "</td></tr>"
                + "<tr><td style='padding:8px; background:#f3f4f6; font-weight:bold;'>Endereço</td>"
                + "<td style='padding:8px;'>" + endereco + "</td></tr>"
                + "</table>"
                + "<p style='color:#666; font-size:14px;'>Acesse o FestApp para tomar uma ação.</p>"
                + "</div>";

        executarEnvio(emailDona, "⚠️ Atraso: " + nomeFuncionario + " — " + nomeCliente, html);
    }

    public void enviarAlertaFestaProxima(String emailDona, String nomeCliente,
                                         String dataHora, String endereco, int horas) {

        nomeCliente = safe(nomeCliente);
        dataHora    = safe(dataHora);
        endereco    = safe(endereco);

        String html = "<div style='font-family: sans-serif; max-width: 600px;'>"
                + "<h2 style='color: #1e3a8a;'>🎉 Festa em " + horas + "h — FestApp</h2>"
                + "<p>Você tem uma festa se aproximando!</p>"
                + "<table style='width:100%; border-collapse:collapse; margin:16px 0;'>"
                + "<tr><td style='padding:8px; background:#f3f4f6; font-weight:bold;'>Cliente</td>"
                + "<td style='padding:8px;'>" + nomeCliente + "</td></tr>"
                + "<tr><td style='padding:8px; background:#f3f4f6; font-weight:bold;'>Data e Hora</td>"
                + "<td style='padding:8px;'>" + dataHora + "</td></tr>"
                + "<tr><td style='padding:8px; background:#f3f4f6; font-weight:bold;'>Endereço</td>"
                + "<td style='padding:8px;'>" + endereco + "</td></tr>"
                + "</table>"
                + "<p style='color:#666; font-size:14px;'>Acesse o FestApp para verificar a equipe escalada.</p>"
                + "</div>";

        executarEnvio(emailDona, "🎉 Festa em " + horas + "h: " + nomeCliente, html);
    }

    private void executarEnvio(String para, String assunto, String html) {
        // Validação básica de email antes de disparar
        if (para == null || !para.contains("@")) {
            log.warn("Email inválido ignorado: {}", para);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = Map.of(
                "from", "FestApp <onboarding@resend.dev>",
                "to", para,
                "subject", assunto,
                "html", html
        );

        try {
            restTemplate.postForEntity(
                    "https://api.resend.com/emails",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            log.info("Email enviado para: {}", para);
        } catch (Exception e) {
            // Fix: não expõe detalhes do erro — apenas loga internamente
            log.error("Falha ao enviar email para {}: {}", para, e.getMessage());
        }
    }
}