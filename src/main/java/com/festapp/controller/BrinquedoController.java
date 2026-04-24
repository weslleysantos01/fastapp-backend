package com.festapp.controller;

import com.festapp.model.*;
import com.festapp.service.BrinquedoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.festapp.model.StatusBrinquedo;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brinquedos")
@CrossOrigin(origins = "http://localhost:3000")
public class BrinquedoController {

    @Autowired
    private BrinquedoService brinquedoService;

    private Long getEmpresaId(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        if (empresaId == null) throw new RuntimeException("Não autorizado");
        return empresaId;
    }

    @GetMapping
    public ResponseEntity<List<Brinquedo>> listar(HttpServletRequest request) {
        return ResponseEntity.ok(brinquedoService.listarTodos(getEmpresaId(request)));
    }

    @PostMapping
    public ResponseEntity<Brinquedo> cadastrar(@Valid @RequestBody Brinquedo brinquedo, HttpServletRequest request) {
        return ResponseEntity.status(201).body(brinquedoService.cadastrar(brinquedo, getEmpresaId(request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {

        try {
            // Converte a String para o Enum StatusBrinquedo
            StatusBrinquedo statusEnum = StatusBrinquedo.valueOf(status.toUpperCase());
            return ResponseEntity.ok(brinquedoService.atualizarStatus(id, statusEnum, getEmpresaId(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Status inválido: " + status));
        }
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservar(@Valid @RequestBody ReservaRequest req, HttpServletRequest request) {
        try {
            return ResponseEntity.status(201).body(
                    brinquedoService.reservar(req.getFestaId(), req.getBrinquedoId(), req.getQuantidade(), getEmpresaId(request))
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ReservaRequest {
        @NotNull private Long festaId;
        @NotNull private Long brinquedoId;
        @Min(1) private int quantidade;
    }
}