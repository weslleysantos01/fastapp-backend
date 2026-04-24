package com.festapp.service;

import com.festapp.model.*;
import com.festapp.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<Funcionario> listarTodos(Long empresaId) {
        return funcionarioRepository.findByEmpresaId(empresaId);
    }

    public Funcionario cadastrar(Funcionario funcionario, Long empresaId) {
        if (empresaId == null) throw new RuntimeException("Não autorizado");

        funcionario.setEmpresaId(empresaId);

        LocalDate nascimento = funcionario.getDataNascimento();
        if (nascimento == null) {
            throw new RuntimeException("Data de nascimento é obrigatória");
        }

        // Blindagem: valores padrão usando Enums
        funcionario.setAvaliacaoMedia(5.0);
        funcionario.setFestasNoDia(0);
        funcionario.setStatusDia(StatusDia.DISPONIVEL);
        funcionario.setAtivo(true);

        // Lógica de idade mantida, mas agora usando o Enum StatusCadastro
        int idade = Period.between(nascimento, LocalDate.now()).getYears();
        funcionario.setStatusCadastro(idade < 18 ? StatusCadastro.PENDENTE : StatusCadastro.ATIVO);

        return funcionarioRepository.save(funcionario);
    }

    // Corrigido: Agora recebe StatusDia (Enum) em vez de String
    public Funcionario atualizarStatus(Long id, StatusDia statusDia, Long empresaId) {
        Funcionario f = funcionarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        f.setStatusDia(statusDia);
        return funcionarioRepository.save(f);
    }

    public Funcionario aprovar(Long id, Long empresaId) {
        Funcionario f = funcionarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        f.setStatusCadastro(StatusCadastro.ATIVO);
        return funcionarioRepository.save(f);
    }

    public void resetarFestasHoje(Long empresaId) {
        List<Funcionario> todos = funcionarioRepository.findByEmpresaId(empresaId);
        for (Funcionario f : todos) {
            f.setFestasNoDia(0);
            f.setStatusDia(StatusDia.DISPONIVEL);
            funcionarioRepository.save(f);
        }
    }
}