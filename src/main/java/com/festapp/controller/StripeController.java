package com.festapp.controller;

import com.festapp.service.StripeService;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/stripe")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest req, HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        try {
            String url = stripeService.criarCheckout(empresaId, req.getPlano());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao processar checkout"));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(HttpServletRequest request,
                                     @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            byte[] payloadBytes = request.getInputStream().readAllBytes();
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);

            // Validação real da assinatura do Stripe - Protege contra falsificação
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            stripeService.processarWebhook(event);

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
}

@Data
class CheckoutRequest {
    private String plano;
}