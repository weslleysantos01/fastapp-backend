package com.festapp.controller;

import com.festapp.model.Empresa;
import com.festapp.repository.EmpresaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/empresa")
@CrossOrigin(origins = "http://localhost:3000")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @GetMapping("/perfil")
    public ResponseEntity<?> getPerfil() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return empresaRepository.findByEmail(email)
                .map(empresa -> ResponseEntity.ok(Map.of(
                        "id", empresa.getId(),
                        "nome", empresa.getNome(),
                        "email", empresa.getEmail(),
                        "telefone", empresa.getTelefone() != null ? empresa.getTelefone() : "",
                        "plano", empresa.getPlano().name(),
                        "statusAssinatura", empresa.getStatusAssinatura().name()
                )))
                .orElse(ResponseEntity.status(404).build());
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> atualizarPerfil(@Valid @RequestBody PerfilRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        empresa.setNome(request.getNome());
        empresa.setTelefone(request.getTelefone());
        empresaRepository.save(empresa);

        return ResponseEntity.ok(Map.of("mensagem", "Perfil atualizado com sucesso"));
    }
}

@Data
class PerfilRequest {
    @NotBlank @Size(min = 2, max = 255) private String nome;
    @Size(max = 20) private String telefone;
}