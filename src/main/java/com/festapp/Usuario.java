package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private UUID id; // UUID do Supabase Auth

    @NotBlank
    @Size(min = 2, max = 255)
    private String nome;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Pattern(regexp = "DONA|FUNCIONARIO")
    private String perfil;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    private Boolean ativo = true;

    @OneToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;
}