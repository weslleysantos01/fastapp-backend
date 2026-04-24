package com.festapp.model;

import com.festapp.model.enums.PlanoTipo;
import com.festapp.model.enums.StatusAssinatura;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 255)
    private String nome;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanoTipo plano = PlanoTipo.BASICO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_assinatura", nullable = false)
    private StatusAssinatura statusAssinatura = StatusAssinatura.INATIVO;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;
}