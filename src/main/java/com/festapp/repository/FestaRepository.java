package com.festapp.repository;

import com.festapp.model.Festa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestaRepository extends JpaRepository<Festa, Long> {
}