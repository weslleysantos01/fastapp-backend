package com.festapp.controller;

import com.festapp.model.Escala;
import com.festapp.model.Festa;
import com.festapp.service.EscalaService;
import com.festapp.service.FestaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // IMPORTANTE: Faltava esse cara para o Map.of funcionar

@RestController
@RequestMapping("/festas")
@CrossOrigin(origins = "http://localhost:3000")
public class FestaController {

    @Autowired
    private FestaService festaService;

    @Autowired
    private EscalaService escalaService;

    @GetMapping
    public List<Festa> listar(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        return festaService.listarTodas(empresaId);
    }

    @PostMapping
    public ResponseEntity<Festa> cadastrar(
            @Valid @RequestBody Festa festa,
            HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        return ResponseEntity.status(201).body(festaService.cadastrar(festa, empresaId));
    }

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarFuncionario(
            @RequestParam Long festaId,
            @RequestParam Long funcionarioId,
            HttpServletRequest request) {

        Long empresaId = (Long) request.getAttribute("empresa_id");

        try {
            // Chamando o método correto que criamos no EscalaService
            Escala escala = escalaService.adicionarFuncionario(festaId, funcionarioId, empresaId);
            return ResponseEntity.status(201).body(escala);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
} // Chave que fecha a classe (faltava no seu código)