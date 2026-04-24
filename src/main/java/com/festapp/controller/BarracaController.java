package com.festapp.controller;

import com.festapp.model.Barraca;
import com.festapp.repository.BarracaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/barracas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BarracaController {

    @Autowired
    private BarracaRepository barracaRepository;

    // Dona lista as barracas da empresa dela
    @GetMapping
    public List<Barraca> listar(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        return barracaRepository.findByEmpresaId(empresaId);
    }

    // Dona cadastra nova barraca
    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody Barraca barraca,
            HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        barraca.setEmpresaId(empresaId);
        return ResponseEntity.status(201).body(barracaRepository.save(barraca));
    }

    // Dona deleta barraca
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");
        Barraca b = barracaRepository.findById(id)
                .filter(bar -> bar.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new RuntimeException("Barraca não encontrada"));
        barracaRepository.delete(b);
        return ResponseEntity.ok(Map.of("mensagem", "Barraca removida"));
    }

    // Público — funcionário busca barracas pelo empresaId no cadastro
    @GetMapping("/tipos")
    public List<String> tipos(@RequestParam Long empresaId) {
        return barracaRepository.findByEmpresaId(empresaId)
                .stream()
                .map(Barraca::getNome)
                .toList();
    }
}