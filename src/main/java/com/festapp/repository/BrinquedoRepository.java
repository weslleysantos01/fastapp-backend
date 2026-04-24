package com.festapp.repository;

import com.festapp.model.Brinquedo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrinquedoRepository extends JpaRepository<Brinquedo, Long> {
    List<Brinquedo> findByEmpresaId(Long empresaId);
    Optional<Brinquedo> findByIdAndEmpresaId(Long id, Long empresaId);
}