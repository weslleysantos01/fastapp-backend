package com.festapp.repository;

import com.festapp.model.Festa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FestaRepository extends JpaRepository<Festa, Long> {

    List<Festa> findByEmpresaId(Long empresaId);

    Optional<Festa> findByIdAndEmpresaId(Long id, Long empresaId);

    // Substitui CAST(dataHora AS date) que não funciona no MySQL com Hibernate 6
    @Query("""
        SELECT f FROM Festa f
        WHERE f.empresaId = :empresaId
        AND f.dataHora >= :inicioDia AND f.dataHora < :fimDia
    """)
    List<Festa> findFestasHoje(
            @Param("empresaId") Long empresaId,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("fimDia") LocalDateTime fimDia
    );

    @Query("""
        SELECT f FROM Festa f
        WHERE f.empresaId = :empresaId
        AND f.dataHora >= :inicio AND f.dataHora <= :fim
    """)
    List<Festa> findByEmpresaIdAndDataHoraBetween(
            @Param("empresaId") Long empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
        SELECT f FROM Festa f
        WHERE f.dataHora >= :inicio AND f.dataHora <= :fim
    """)
    List<Festa> findByDataHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Últimas 5 festas — ordenado no banco, sem carregar tudo em memória
    List<Festa> findTop5ByEmpresaIdOrderByDataHoraDesc(Long empresaId);
}