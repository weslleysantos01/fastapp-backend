package com.festapp.repository;

import com.festapp.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByDisponivelTrue();

    List<Funcionario> findByDisponivelTrueAndFestaHojeLessThan(int limite);
}
