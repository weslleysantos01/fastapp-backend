package com.festapp.service;

import com.festapp.model.*;
import com.festapp.repository.EscalaRepository;
import com.festapp.repository.FestaRepository;
import com.festapp.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalaService {

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private FestaRepository festaRepository;

    public List<Escala> listarPorFesta(Long festaId, Long empresaId) {
        return escalaRepository.findByFestaId(festaId).stream()
                .filter(e -> e.getFesta().getEmpresaId().equals(empresaId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Escala adicionarFuncionario(Long festaId, Long funcionarioId, Long empresaId) {
        Festa festa = festaRepository.findByIdAndEmpresaId(festaId, empresaId)
                .orElseThrow(() -> new RuntimeException("Festa não encontrada"));

        Funcionario func = funcionarioRepository.findByIdAndEmpresaId(funcionarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        // Regra de Negócio: Funcionário precisa estar ATIVO (usando Enum)
        if (func.getStatusCadastro() != StatusCadastro.ATIVO) {
            throw new RuntimeException("Funcionário com cadastro pendente ou inativo");
        }

        // Regra: Não escala quem já está ocupado ou em folga
        if (func.getStatusDia() == StatusDia.OCUPADO || func.getStatusDia() == StatusDia.FOLGA) {
            throw new RuntimeException("Funcionário indisponível para este dia");
        }

        Escala escala = new Escala();
        escala.setFesta(festa);
        escala.setFuncionario(func);

        // Atualiza status do funcionário para OCUPADO
        func.setStatusDia(StatusDia.OCUPADO);
        func.setFestasNoDia(func.getFestasNoDia() + 1);
        funcionarioRepository.save(func);

        return escalaRepository.save(escala);
    }

    @Transactional
    public void removerFuncionario(Long escalaId, Long empresaId) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new RuntimeException("Escala não encontrada"));

        if (!escala.getFesta().getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("Não autorizado");
        }

        Funcionario func = escala.getFuncionario();

        // Se for a última festa dele no dia, volta para DISPONIVEL
        if (func.getFestasNoDia() <= 1) {
            func.setStatusDia(StatusDia.DISPONIVEL);
            func.setFestasNoDia(0);
        } else {
            func.setFestasNoDia(func.getFestasNoDia() - 1);
        }

        funcionarioRepository.save(func);
        escalaRepository.delete(escala);
    }
}