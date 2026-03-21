package com.festapp.controller;

import com.festapp.model.Escala;
import com.festapp.model.Usuario;
import com.festapp.repository.EscalaRepository;
import com.festapp.repository.UsuarioRepository;
import com.festapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/escalas")
@CrossOrigin(origins = "http://localhost:3000")
public class EscalaController {

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/festa/{festaId}")
    public List<Escala> listarPorFesta(@PathVariable Long festaId) {
        return escalaRepository.findByFestaId(festaId);
    }

    @GetMapping("/funcionario/{funcionarioId}")
    public List<Escala> listarPorFuncionario(@PathVariable Long funcionarioId) {
        return escalaRepository.findByFuncionarioId(funcionarioId);
    }

    @GetMapping("/minhas")
    public List<Escala> minhasEscalas(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extrairEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getFuncionario() == null) {
            return new ArrayList<>();
        }

        return escalaRepository.findByFuncionarioId(usuario.getFuncionario().getId());
    }

    @PatchMapping("/{id}/chegada")
    public Escala registrarChegada(@PathVariable Long id) {
        Escala escala = escalaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escala não encontrada"));

        LocalDateTime agora = LocalDateTime.now();
        escala.setHoraChegada(agora);

        LocalDateTime limiteTolerancia = escala.getFesta()
                .getHorario().plusMinutes(10);

        if (agora.isAfter(limiteTolerancia)) {
            escala.setStatusPonto("ATRASADO");
        } else {
            escala.setStatusPonto("NO_PRAZO");
        }

        return escalaRepository.save(escala);
    }
}