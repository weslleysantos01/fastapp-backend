package com.festapp.controller;

import com.festapp.model.Empresa;
import com.festapp.model.Usuario;
import com.festapp.repository.EmpresaRepository;
import com.festapp.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.role.key}")
    private String supabaseServiceKey;

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return empresaRepository.findByEmail(email)
                .map(empresa -> ResponseEntity.ok(Map.of(
                        "plano", empresa.getPlano().name(),
                        "statusAssinatura", empresa.getStatusAssinatura().name()
                )))
                .orElse(ResponseEntity.status(404).build());
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody CadastroRequest request) {

        // Validação básica de senha
        if (request.getSenha().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Senha fraca"));
        }

        // Verifica duplicidade
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()
                || empresaRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(400).body(Map.of("erro", "Email já cadastrado"));
        }

        // Cria empresa
        Empresa empresa = new Empresa();
        empresa.setNome(request.getNomeEmpresa());
        empresa.setEmail(request.getEmail());
        empresa.setTelefone(request.getTelefone());
        empresa = empresaRepository.save(empresa);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(supabaseServiceKey);
        headers.set("apikey", supabaseServiceKey);

        Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("empresa_id", empresa.getId());
        appMetadata.put("perfil", "DONA");

        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getSenha());
        body.put("app_metadata", appMetadata);
        body.put("email_confirm", true);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    supabaseUrl + "/auth/v1/admin/users",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            // 🔒 Validação segura da resposta
            if (response.getBody() == null || !response.getBody().containsKey("id")) {
                empresaRepository.delete(empresa);
                return ResponseEntity.status(500)
                        .body(Map.of("erro", "Falha ao criar usuário externo"));
            }

            Object idObj = response.getBody().get("id");

            if (!(idObj instanceof String supabaseId)) {
                empresaRepository.delete(empresa);
                return ResponseEntity.status(500)
                        .body(Map.of("erro", "ID inválido retornado pelo provedor"));
            }

            // Cria usuário local
            Usuario usuario = new Usuario();
            usuario.setId(UUID.fromString(supabaseId));
            usuario.setNome(request.getNomeDona());
            usuario.setEmail(request.getEmail());
            usuario.setPerfil("DONA");
            usuario.setEmpresaId(empresa.getId());

            usuarioRepository.save(usuario);

        } catch (Exception e) {
            empresaRepository.delete(empresa);

            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao processar cadastro"));
        }

        return ResponseEntity.status(201).body(Map.of(
                "mensagem", "Empresa cadastrada com sucesso",
                "empresa_id", empresa.getId()
        ));
    }
}

@Data
class CadastroRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String nomeDona;

    @NotBlank
    @Size(min = 2, max = 255)
    private String nomeEmpresa;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 128)
    private String senha;

    @Size(max = 20)
    private String telefone;

    private String plano;
}