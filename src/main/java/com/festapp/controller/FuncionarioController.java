package com.festapp.controller;

import com.festapp.model.*;
import com.festapp.service.FuncionarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    // Helper para extrair o ID da empresa do request (injetado pelo seu Interceptor/Filtro)
    private Long getEmpresaId(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        if (empresaId == null) {
            throw new RuntimeException("Acesso não autorizado");
        }
        return empresaId;
    }

    @GetMapping
    public ResponseEntity<List<Funcionario>> listar(HttpServletRequest request) {
        return ResponseEntity.ok(funcionarioService.listarTodos(getEmpresaId(request)));
    }

    @PostMapping
    public ResponseEntity<Funcionario> cadastrar(
            @Valid @RequestBody Funcionario funcionario,
            HttpServletRequest request) {
        return ResponseEntity.status(201)
                .body(funcionarioService.cadastrar(funcionario, getEmpresaId(request)));
    }

    @PatchMapping("/{id}/status-dia")
    public ResponseEntity<?> atualizarStatusDia(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {

        try {
            // Converte a String para o Enum StatusDia (Ex: "FOLGA")
            StatusDia statusEnum = StatusDia.valueOf(status.toUpperCase());
            return ResponseEntity.ok(
                    funcionarioService.atualizarStatus(id, statusEnum, getEmpresaId(request))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Status de dia inválido: " + status));
        }
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Funcionario> aprovar(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(funcionarioService.aprovar(id, getEmpresaId(request)));
    }

    @PostMapping("/resetar-dia")
    public ResponseEntity<?> resetarDia(HttpServletRequest request) {
        funcionarioService.resetarFestasHoje(getEmpresaId(request));
        return ResponseEntity.ok(Map.of("mensagem", "Status dos funcionários resetado com sucesso"));
    }
}