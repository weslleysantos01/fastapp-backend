package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    private boolean disponivel = true;

    @Min(value = 0, message = "Festas hoje não pode ser negativo")
    @Max(value = 2, message = "Máximo de 2 festas por dia")
    private int festaHoje = 0;
}