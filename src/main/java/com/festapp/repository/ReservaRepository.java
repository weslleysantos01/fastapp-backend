package com.festapp.repository;

import com.festapp.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByFestaId(Long festaId);

    // Fix: busca reserva existente para festa+brinquedo — evita duplicata
    Optional<Reserva> findByFestaIdAndBrinquedoId(Long festaId, Long brinquedoId);

    @Query("""
        SELECT r FROM Reserva r
        WHERE r.brinquedo.id = :brinquedoId
        AND r.festa.dataHora < :fim
        AND r.festa.dataHora >= :inicio
    """)
    List<Reserva> findConflitos(
            @Param("brinquedoId") Long brinquedoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}