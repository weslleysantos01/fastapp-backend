package com.festapp.controller;

import com.festapp.model.Funcionario;
import com.festapp.service.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/funcionarios")
@CrossOrigin(origins = "http://localhost:3000")
public class FuncionarioController {

    @Autowired
    private FuncionarioService funcionarioService;

    @GetMapping
    public List<Funcionario> listar() {
        return funcionarioService.listarTodos();
    }

    @PostMapping
    public Funcionario cadastrar(@RequestBody Funcionario funcionario) {
        return funcionarioService.cadastrar(funcionario);
    }

    @PatchMapping("/{id}/disponibilidade")
    public Funcionario atualizarDisponibilidade(
            @PathVariable Long id,
            @RequestParam boolean disponivel) {
        return funcionarioService.atualizarDisponibilidade(id, disponivel);
    }
}