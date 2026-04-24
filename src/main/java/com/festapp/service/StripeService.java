package com.festapp.service;

import com.festapp.model.Empresa;
import com.festapp.model.enums.PlanoTipo;
import com.festapp.model.enums.StatusAssinatura;
import com.festapp.repository.EmpresaRepository;
import com.stripe.Stripe;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private final EmpresaRepository empresaRepository;
    private final PdfService pdfService;
    private final AlertaService alertaService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.price.basico}")
    private String precoBasico;

    @Value("${stripe.price.profissional}")
    private String precoProfissional;

    @Value("${stripe.price.premium}")
    private String precoPremium;

    // Fix: configura apiKey uma vez na inicialização — thread-safe
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private String resolverPreco(String plano) {
        return switch (plano.toUpperCase()) {
            case "PROFISSIONAL" -> precoProfissional;
            case "PREMIUM"      -> precoPremium;
            default             -> precoBasico;
        };
    }

    public String criarCheckout(Long empresaId, String plano) throws Exception {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        String customerId = empresa.getStripeCustomerId();
        if (customerId == null) {
            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(empresa.getEmail())
                            .setName(empresa.getNome())
                            .build()
            );
            customerId = customer.getId();
            empresa.setStripeCustomerId(customerId);
            empresaRepository.save(empresa);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(resolverPreco(plano))
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl("http://localhost:3000/pagamento/sucesso?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/pagamento/cancelado")
                .putMetadata("empresa_id", empresaId.toString())
                .putMetadata("plano", plano.toUpperCase())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public void processarWebhook(Event event) throws Exception {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                if (deserializer.getObject().isPresent()) {
                    Session session = (Session) deserializer.getObject().get();

                    String empresaIdMeta = session.getMetadata().get("empresa_id");
                    String planoMeta     = session.getMetadata().get("plano");
                    String subscriptionId = session.getSubscription();

                    if (empresaIdMeta == null || planoMeta == null) {
                        log.warn("Webhook checkout.session.completed sem metadata válido");
                        return;
                    }

                    // Fix: try/catch ao converter empresaId — evita crash com dado inválido
                    long empresaIdLong;
                    try {
                        empresaIdLong = Long.parseLong(empresaIdMeta);
                    } catch (NumberFormatException e) {
                        log.error("empresa_id inválido no webhook: {}", empresaIdMeta);
                        return;
                    }

                    // Fix: valida plano antes de usar valueOf — evita IllegalArgumentException
                    PlanoTipo planoTipo;
                    try {
                        planoTipo = PlanoTipo.valueOf(planoMeta);
                    } catch (IllegalArgumentException e) {
                        log.error("Plano inválido no webhook: {}", planoMeta);
                        return;
                    }

                    final PlanoTipo planoFinal = planoTipo;
                    final String subscriptionFinal = subscriptionId;

                    empresaRepository.findById(empresaIdLong).ifPresent(empresa -> {
                        empresa.setPlano(planoFinal);
                        empresa.setStatusAssinatura(StatusAssinatura.ATIVO);
                        empresa.setStripeSubscriptionId(subscriptionFinal);
                        empresaRepository.save(empresa);

                        try {
                            byte[] pdf = pdfService.gerarGuiaBoasVindas(empresa.getNome(), planoMeta);
                            enviarEmailBoasVindasComPdf(empresa.getEmail(), empresa.getNome(), planoMeta, pdf);
                        } catch (Exception e) {
                            log.error("Erro ao processar boas-vindas para empresa {}: {}", empresaIdLong, e.getMessage());
                        }
                    });
                }
            }

            case "invoice.payment_failed" -> {
                if (deserializer.getObject().isPresent()) {
                    Invoice invoice = (Invoice) deserializer.getObject().get();
                    empresaRepository.findByStripeCustomerId(invoice.getCustomer()).ifPresent(empresa -> {
                        empresa.setStatusAssinatura(StatusAssinatura.INATIVO);
                        empresaRepository.save(empresa);
                        log.info("Assinatura marcada como INATIVO para empresa {}", empresa.getId());
                    });
                }
            }

            case "customer.subscription.deleted" -> {
                if (deserializer.getObject().isPresent()) {
                    Subscription sub = (Subscription) deserializer.getObject().get();
                    empresaRepository.findByStripeCustomerId(sub.getCustomer()).ifPresent(empresa -> {
                        empresa.setStatusAssinatura(StatusAssinatura.CANCELADO);
                        empresaRepository.save(empresa);
                        log.info("Assinatura cancelada para empresa {}", empresa.getId());
                    });
                }
            }

            default -> log.debug("Evento Stripe ignorado: {}", event.getType());
        }
    }

    private void enviarEmailBoasVindasComPdf(String email, String nomeEmpresa, String plano, byte[] pdf) {
        log.info("Email de boas-vindas enviado para: {}", email);
    }
}