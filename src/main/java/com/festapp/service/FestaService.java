package com.festapp.service;

import com.festapp.model.Festa;
import com.festapp.repository.FestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FestaService {

    @Autowired
    private FestaRepository festaRepository;

    public List<Festa> listarTodas() {
        return festaRepository.findAll();
    }

    public Festa cadastrar(Festa festa) {
        return festaRepository.save(festa);
    }

    public Festa buscarPorId(Long id) {
        return festaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festa não encontrada"));
    }
}