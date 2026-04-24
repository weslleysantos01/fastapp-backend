package com.festapp.repository;

import com.festapp.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByEmail(String email);

    Optional<Empresa> findByEmail(String email);

    Optional<Empresa> findByStripeCustomerId(String stripeCustomerId);
}