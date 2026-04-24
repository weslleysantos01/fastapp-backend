package com.festapp.service;

import com.festapp.model.*;
import com.festapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import com.festapp.model.StatusBrinquedo;

@Service
public class BrinquedoService {

    @Autowired
    private BrinquedoRepository brinquedoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private FestaRepository festaRepository;

    public List<Brinquedo> listarTodos(Long empresaId) {
        if (empresaId == null) throw new RuntimeException("Não autorizado");
        return brinquedoRepository.findByEmpresaId(empresaId);
    }

    public Brinquedo cadastrar(Brinquedo brinquedo, Long empresaId) {
        if (empresaId == null) throw new RuntimeException("Não autorizado");
        brinquedo.setEmpresaId(empresaId);
        return brinquedoRepository.save(brinquedo);
    }

    public Brinquedo atualizarStatus(Long id, StatusBrinquedo status, Long empresaId) {
        if (empresaId == null) throw new RuntimeException("Não autorizado");

        Brinquedo brinquedo = brinquedoRepository.findById(id)
                .filter(b -> b.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new RuntimeException("Brinquedo não encontrado"));

        brinquedo.setStatus(status);
        return brinquedoRepository.save(brinquedo);
    }

    @Transactional
    public Reserva reservar(Long festaId, Long brinquedoId, int quantidade, Long empresaId) {
        Festa festa = festaRepository.findByIdAndEmpresaId(festaId, empresaId)
                .orElseThrow(() -> new RuntimeException("Festa não encontrada"));

        Brinquedo brinquedo = brinquedoRepository.findById(brinquedoId)
                .filter(b -> b.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new RuntimeException("Brinquedo não encontrado"));

        LocalDateTime inicio = festa.getDataHora();
        LocalDateTime fim = inicio.plusHours(festa.getDuracaoHoras());

        List<Reserva> conflitos = reservaRepository.findConflitos(brinquedoId, inicio.minusHours(12), fim);

        int qtdReservada = conflitos.stream()
                .filter(r -> r.getFesta() != null && r.getFesta().getEmpresaId().equals(empresaId))
                .mapToInt(Reserva::getQuantidade)
                .sum();

        if ((brinquedo.getQuantidade() - qtdReservada) < quantidade) {
            throw new RuntimeException("Brinquedo indisponível para este horário");
        }

        Reserva reserva = new Reserva();
        reserva.setFesta(festa);
        reserva.setBrinquedo(brinquedo);
        reserva.setQuantidade(quantidade);

        return reservaRepository.save(reserva);
    }

    public List<Reserva> listarReservasPorFesta(Long festaId, Long empresaId) {
        return reservaRepository.findByFestaId(festaId).stream()
                .filter(r -> r.getFesta().getEmpresaId().equals(empresaId))
                .toList();
    }
}