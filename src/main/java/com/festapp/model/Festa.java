package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "festas")
public class Festa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotNull(message = "Horário é obrigatório")
    @Future(message = "Horário deve ser no futuro")
    private LocalDateTime horario;

    @Min(value = 1, message = "Mínimo de 1 funcionário por festa")
    @Max(value = 20, message = "Máximo de 20 funcionários por festa")
    private int qtdFuncionarios;

    private String status = "AGUARDANDO";

    @ManyToMany
    @JoinTable(
            name = "escalas",
            joinColumns = @JoinColumn(name = "festa_id"),
            inverseJoinColumns = @JoinColumn(name = "funcionario_id")
    )
    private List<Funcionario> funcionariosEscalados;
}