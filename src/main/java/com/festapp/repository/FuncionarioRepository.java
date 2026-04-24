package com.festapp.repository;

import com.festapp.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByEmpresaId(Long empresaId);

    Optional<Funcionario> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Funcionario> findByEmail(String email);

    Optional<Funcionario> findByEmailAndEmpresaId(String email, Long empresaId);

    List<Funcionario> findByEmpresaIdAndStatusDiaAndFestasNoDiaLessThan(
            Long empresaId, String statusDia, int limite);
}