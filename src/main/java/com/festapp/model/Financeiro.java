package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "financeiro")
public class Financeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne
    @JoinColumn(name = "festa_id", nullable = false)
    private Festa festa;

    @Column(name = "valor_cobrado")
    private Double valorCobrado = 0.0;

    @Column(name = "valor_pago_equipe")
    private Double valorPagoEquipe = 0.0;

    @Column(name = "status_pagamento")
    private String statusPagamento = "PENDENTE";
}
