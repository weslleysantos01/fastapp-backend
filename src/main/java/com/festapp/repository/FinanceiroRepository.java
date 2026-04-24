package com.festapp.repository;

import com.festapp.model.Financeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinanceiroRepository extends JpaRepository<Financeiro, Long> {

    // 🔒 Listagem isolada por empresa
    List<Financeiro> findByEmpresaId(Long empresaId);

    // 🔒 Busca por festa dentro da empresa
    Optional<Financeiro> findByFestaIdAndEmpresaId(Long festaId, Long empresaId);

    // 🔥 CRÍTICO: evita acesso por ID de outra empresa
    Optional<Financeiro> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("""
        SELECT COALESCE(SUM(f.valorCobrado), 0.0) FROM Financeiro f
        WHERE f.empresaId = :empresaId AND f.statusPagamento = 'PAGO'
    """)
    Double totalReceita(@Param("empresaId") Long empresaId);

    @Query("""
        SELECT COALESCE(SUM(f.valorPagoEquipe), 0.0) FROM Financeiro f
        WHERE f.empresaId = :empresaId AND f.statusPagamento = 'PAGO'
    """)
    Double totalCusto(@Param("empresaId") Long empresaId);

    @Query("""
        SELECT COALESCE(SUM(f.valorCobrado), 0.0) FROM Financeiro f
        WHERE f.empresaId = :empresaId
        AND f.statusPagamento = 'PAGO'
        AND MONTH(f.festa.dataHora) = :mes
        AND YEAR(f.festa.dataHora) = :ano
    """)
    Double totalReceitaMes(@Param("empresaId") Long empresaId,
                           @Param("mes") int mes,
                           @Param("ano") int ano);

    @Query("""
        SELECT MONTH(f.festa.dataHora) as mes, COALESCE(SUM(f.valorCobrado), 0) as total
        FROM Financeiro f
        WHERE f.empresaId = :empresaId
        AND f.statusPagamento = 'PAGO'
        AND YEAR(f.festa.dataHora) = :ano
        GROUP BY MONTH(f.festa.dataHora)
        ORDER BY MONTH(f.festa.dataHora)
    """)
    List<Object[]> receitaPorMes(@Param("empresaId") Long empresaId,
                                 @Param("ano") int ano);
}