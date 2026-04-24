package com.festapp.controller;

import com.festapp.model.Financeiro;
import com.festapp.model.enums.PlanoTipo;
import com.festapp.repository.EmpresaRepository;
import com.festapp.service.FinanceiroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/financeiro")
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceiroController {

    @Autowired
    private FinanceiroService financeiroService;

    @Autowired
    private EmpresaRepository empresaRepository;

    // Verifica se empresa tem plano Pro ou acima
    private boolean temAcessoFinanceiro(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .map(e -> e.getPlano() == PlanoTipo.PROFISSIONAL || e.getPlano() == PlanoTipo.PREMIUM)
                .orElse(false);
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        if (!temAcessoFinanceiro(empresaId)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Plano não inclui acesso ao financeiro"));
        }
        return ResponseEntity.ok(financeiroService.listarTodos(empresaId));
    }

    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        if (!temAcessoFinanceiro(empresaId)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Plano não inclui acesso ao financeiro"));
        }
        return ResponseEntity.ok(financeiroService.resumo(empresaId));
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @Valid @RequestBody FinanceiroRequest req,
            HttpServletRequest request) {

        Long empresaId = (Long) request.getAttribute("empresa_id");

        if (empresaId == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autorizado"));
        }

        if (!temAcessoFinanceiro(empresaId)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Plano não inclui acesso ao financeiro"));
        }

        Financeiro fin = financeiroService.salvar(
                req.getFestaId(),
                req.getValorCobrado(),
                req.getValorPagoEquipe(),
                empresaId
        );

        return ResponseEntity.status(201).body(fin);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {

        Long empresaId = (Long) request.getAttribute("empresa_id");

        if (empresaId == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autorizado"));
        }

        if (!temAcessoFinanceiro(empresaId)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Plano não inclui acesso ao financeiro"));
        }

        if (!List.of("PENDENTE", "PAGO", "CANCELADO").contains(status)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Status inválido"));
        }

        return ResponseEntity.ok(
                financeiroService.atualizarStatus(id, status, empresaId)
        );
    }
}

@Data
class FinanceiroRequest {

    @NotNull
    private Long festaId;

    @NotNull
    @DecimalMin("0.0")
    private Double valorCobrado;

    @DecimalMin("0.0")
    private Double valorPagoEquipe;
}