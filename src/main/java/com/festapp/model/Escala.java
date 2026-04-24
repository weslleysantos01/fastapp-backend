package com.festapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "escalas")
public class Escala {

    @Column(name = "funcao_na_festa")
    private String funcaoNaFesta; // "BARRACA" ou "GERAL"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "festa_id", nullable = false)
    private Festa festa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "hora_chegada")
    private java.time.LocalDateTime horaChegada;

    @Column(name = "status_pontualidade")
    private String statusPontualidade;
}