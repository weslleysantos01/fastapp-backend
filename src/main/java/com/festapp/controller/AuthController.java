package com.festapp.controller;

import com.festapp.model.Usuario;
import com.festapp.repository.UsuarioRepository;
import com.festapp.security.JwtUtil;
import com.festapp.security.SecurityLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityLogger securityLogger;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        if (usuarioOpt.isEmpty()) {
            securityLogger.loginFalhou(request.getEmail(), ip);
            return ResponseEntity.status(401).body("Usuário não encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            securityLogger.loginFalhou(request.getEmail(), ip);
            return ResponseEntity.status(401).body("Senha incorreta");
        }

        String token = jwtUtil.gerarToken(usuario.getEmail(), usuario.getPerfil());
        securityLogger.loginSucesso(usuario.getEmail(), ip);

        return ResponseEntity.ok(new LoginResponse(
                token,
                usuario.getPerfil(),
                usuario.getNome(),
                usuario.getId(),
                usuario.getFuncionario() != null ? usuario.getFuncionario().getId() : null
        ));
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body("Email já cadastrado");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }
}

@Data
class LoginRequest {
    private String email;
    private String senha;
}

@Data
class LoginResponse {
    private String token;
    private String perfil;
    private String nome;
    private Long usuarioId;
    private Long funcionarioId;

    public LoginResponse(String token, String perfil, String nome, Long usuarioId, Long funcionarioId) {
        this.token = token;
        this.perfil = perfil;
        this.nome = nome;
        this.usuarioId = usuarioId;
        this.funcionarioId = funcionarioId;
    }
}