package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import com.festapp.model.*;

@Data
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Proteção: ID da empresa não deve vir da rua
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email
    @Column(unique = true)
    private String email;

    private String telefone;

    // --- PROTEÇÕES APLICADAS (ENUMS) ---

    @NotNull(message = "Sexo é obrigatório")
    @Enumerated(EnumType.STRING)
    private SexoFuncionario sexo;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "tipo_barraca")
    private String tipoBarraca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_cadastro")
    private StatusCadastro statusCadastro = StatusCadastro.ATIVO;

    @DecimalMin("0.0") @DecimalMax("5.0")
    @Column(name = "avaliacao_media")
    private Double avaliacaoMedia = 5.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_dia")
    private StatusDia statusDia = StatusDia.DISPONIVEL;

    // --- CAMPOS EXISTENTES ---

    @Column(name = "festas_no_dia")
    private int festasNoDia = 0;

    private Boolean ativo = true;
}