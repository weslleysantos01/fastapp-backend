package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.festapp.model.StatusBrinquedo;

@Data
@Entity
@Table(name = "brinquedos")
public class Brinquedo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Proteção: empresa_id não deve ser alterado via JSON externo no Controller
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String descricao;

    @Min(value = 1, message = "A quantidade mínima deve ser 1")
    private int quantidade = 1;

    @Column(name = "valor_aluguel")
    private Double valorAluguel;

    // Proteção: Tipagem forte para evitar strings maliciosas no banco
    @Enumerated(EnumType.STRING)
    private StatusBrinquedo status = StatusBrinquedo.DISPONIVEL;
}