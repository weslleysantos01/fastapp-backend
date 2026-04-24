package com.festapp.service;

import com.festapp.model.Festa;
import com.festapp.repository.EmpresaRepository;
import com.festapp.repository.FestaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class NotificacaoAgendada {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAgendada.class);

    private final FestaRepository festaRepository;
    private final EmpresaRepository empresaRepository;
    private final AlertaService alertaService;

    // Fix: controla festas já notificadas para evitar envio duplicado a cada hora
    // ConcurrentHashMap.newKeySet() é thread-safe
    private final Set<Long> festasNotificadas = ConcurrentHashMap.newKeySet();

    // Roda a cada hora
    @Scheduled(fixedRate = 3600000)
    public void notificarFestasProximas() {
        LocalDateTime agora = LocalDateTime.now();

        // Janela de 24h (entre 23h e 25h a partir de agora)
        LocalDateTime inicio = agora.plusHours(23);
        LocalDateTime fim = agora.plusHours(25);

        List<Festa> festas = festaRepository.findByDataHoraBetween(inicio, fim);

        for (Festa festa : festas) {

            // Fix: pula festas já notificadas neste ciclo de vida da aplicação
            if (festasNotificadas.contains(festa.getId())) {
                log.debug("Notificação já enviada para festa ID {}, pulando", festa.getId());
                continue;
            }

            empresaRepository.findById(festa.getEmpresaId()).ifPresentOrElse(empresa -> {

                String dataFormatada = festa.getDataHora()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                try {
                    alertaService.enviarAlertaFestaProxima(
                            empresa.getEmail(),
                            festa.getNomeCliente(),
                            dataFormatada,
                            festa.getEndereco(),
                            24
                    );

                    festasNotificadas.add(festa.getId());
                    log.info("Notificação enviada para festa ID {} — cliente: {}",
                            festa.getId(), festa.getNomeCliente());

                } catch (Exception e) {
                    log.error("Erro ao notificar festa ID {}: {}", festa.getId(), e.getMessage());
                }

            }, () -> log.warn("Empresa não encontrada para festa ID: {}", festa.getId()));
        }

        // Limpa festas passadas do set para não crescer indefinidamente
        festasNotificadas.removeIf(id ->
                festaRepository.findById(id)
                        .map(f -> f.getDataHora().isBefore(agora))
                        .orElse(true)
        );
    }
}