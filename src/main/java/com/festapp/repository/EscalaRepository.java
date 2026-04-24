package com.festapp.repository;

import com.festapp.dto.EscalaAtrasadaDTO;
import com.festapp.model.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface EscalaRepository extends JpaRepository<Escala, Long> {

    List<Escala> findByFestaId(Long festaId);

    List<Escala> findByFuncionarioId(Long funcionarioId);

    List<Escala> findByFuncionarioIdAndFesta_EmpresaId(Long funcionarioId, Long empresaId);

    @Query("""
        SELECT new com.festapp.dto.EscalaAtrasadaDTO(
            e.id, f.nomeCliente, f.endereco,
            function('date_format', f.dataHora, '%%d/%%m %%H:%%i'),
            fu.nome, fu.email
        )
        FROM Escala e
        JOIN e.festa f
        JOIN e.funcionario fu
        WHERE e.horaChegada IS NULL
        AND f.dataHora <= CURRENT_TIMESTAMP
        AND (e.statusPontualidade IS NULL OR e.statusPontualidade = '')
        AND f.empresaId = :empresaId
        """)
    List<EscalaAtrasadaDTO> findAtrasadasPorEmpresa(@Param("empresaId") Long empresaId);

    @Query("""
        SELECT e.funcionario.id, COUNT(e.id),
               SUM(CASE WHEN e.statusPontualidade = 'NO_PRAZO' THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.statusPontualidade = 'ATRASADO' THEN 1 ELSE 0 END)
        FROM Escala e
        WHERE e.festa.empresaId = :empresaId
        GROUP BY e.funcionario.id
    """)
    List<Object[]> estatisticasPorFuncionario(@Param("empresaId") Long empresaId);
}