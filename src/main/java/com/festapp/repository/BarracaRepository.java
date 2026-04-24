package com.festapp.repository;

import com.festapp.model.Barraca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BarracaRepository extends JpaRepository<Barraca, Long> {
    List<Barraca> findByEmpresaId(Long empresaId);
}