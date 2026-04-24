package com.festapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "festa_id", nullable = false)
    private Festa festa;

    @ManyToOne
    @JoinColumn(name = "brinquedo_id", nullable = false)
    private Brinquedo brinquedo;

    @Min(1)
    private int quantidade = 1;
}