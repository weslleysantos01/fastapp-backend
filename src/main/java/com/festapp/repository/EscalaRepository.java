package com.festapp.repository;

import com.festapp.model.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EscalaRepository extends JpaRepository<Escala, Long> {

    List<Escala> findByFestaId(Long festaId);

    List<Escala> findByFuncionarioId(Long funcionarioId);
}