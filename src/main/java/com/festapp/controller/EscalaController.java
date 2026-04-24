package com.festapp.controller;

import com.festapp.model.Escala;
import com.festapp.service.EscalaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/escalas")
@CrossOrigin(origins = "http://localhost:3000")
public class EscalaController {

    @Autowired
    private EscalaService escalaService;

    // Helper para capturar o ID da empresa injetado pelo interceptor
    private Long getEmpresaId(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        if (empresaId == null) {
            throw new RuntimeException("Acesso não autorizado");
        }
        return empresaId;
    }

    @GetMapping("/festa/{festaId}")
    public ResponseEntity<List<Escala>> listarPorFesta(
            @PathVariable Long festaId,
            HttpServletRequest request) {
        return ResponseEntity.ok(escalaService.listarPorFesta(festaId, getEmpresaId(request)));
    }

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(
            @RequestParam Long festaId,
            @RequestParam Long funcionarioId,
            HttpServletRequest request) {
        try {
            Escala escala = escalaService.adicionarFuncionario(festaId, funcionarioId, getEmpresaId(request));
            return ResponseEntity.status(201).body(escala);
        } catch (RuntimeException e) {
            // Retorna o erro de negócio (ex: "Funcionário indisponível") de forma limpa
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerFuncionario(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            escalaService.removerFuncionario(id, getEmpresaId(request));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}