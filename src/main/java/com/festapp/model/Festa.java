package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "festas")
public class Festa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tem_barraca", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean temBarraca = false;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Column(name = "nome_cliente")
    private String nomeCliente;

    @NotNull(message = "Data e horário são obrigatórios")
    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Min(1) @Max(12)
    @Column(name = "duracao_horas", columnDefinition = "INT DEFAULT 4")
    private int duracaoHoras = 4;

    private String endereco;

    @Min(1)
    @Column(name = "qtd_funcionarios_necessarios", columnDefinition = "INT DEFAULT 1")
    private int qtdFuncionariosNecessarios = 1;

    private String status = "AGENDADA";
}