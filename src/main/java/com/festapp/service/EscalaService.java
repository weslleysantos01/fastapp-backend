package com.festapp.service;

import com.festapp.model.Escala;
import com.festapp.model.Festa;
import com.festapp.model.Funcionario;
import com.festapp.repository.EscalaRepository;
import com.festapp.repository.FestaRepository;
import com.festapp.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalaService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private FestaRepository festaRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    public List<Escala> alocarEquipe(Long festaId) {

        // Busca a festa
        Festa festa = festaRepository.findById(festaId)
                .orElseThrow(() -> new RuntimeException("Festa não encontrada"));

        // Busca funcionários disponíveis com menos de 2 festas hoje
        List<Funcionario> disponiveis = funcionarioRepository
                .findByDisponivelTrueAndFestaHojeLessThan(2);

        // Ordena por quem tem menos festas no dia
        disponiveis.sort((a, b) -> a.getFestaHoje() - b.getFestaHoje());

        // Verifica se tem gente suficiente
        if (disponiveis.size() < festa.getQtdFuncionarios()) {
            throw new RuntimeException("Funcionários insuficientes! Disponíveis: "
                    + disponiveis.size() + ", Necessários: " + festa.getQtdFuncionarios());
        }

        // Seleciona os primeiros da lista
        List<Funcionario> selecionados = disponiveis.subList(0, festa.getQtdFuncionarios());

        // Cria as escalas e atualiza o contador de cada funcionário
        for (Funcionario f : selecionados) {
            Escala escala = new Escala();
            escala.setFesta(festa);
            escala.setFuncionario(f);
            escalaRepository.save(escala);

            f.setFestaHoje(f.getFestaHoje() + 1);
            funcionarioRepository.save(f);
        }

        festa.setStatus("ESCALADA");
        festaRepository.save(festa);

        return escalaRepository.findByFestaId(festaId);
    }
}