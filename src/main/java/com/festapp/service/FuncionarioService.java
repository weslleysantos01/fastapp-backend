package com.festapp.service;

import com.festapp.model.Funcionario;
import com.festapp.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    public Funcionario cadastrar(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }

    public Funcionario atualizarDisponibilidade(Long id, boolean disponivel) {
        Funcionario f = funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        f.setDisponivel(disponivel);
        return funcionarioRepository.save(f);
    }

    public void resetarFestasHoje() {
        List<Funcionario> todos = funcionarioRepository.findAll();
        for (Funcionario f : todos) {
            f.setFestaHoje(0);
        }
        funcionarioRepository.saveAll(todos);
    }
}