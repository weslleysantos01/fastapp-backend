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

    public List<Festa> listarTodas(Long empresaId) {
        return festaRepository.findByEmpresaId(empresaId);
    }

    public Festa cadastrar(Festa festa, Long empresaId) {
        festa.setEmpresaId(empresaId);
        return festaRepository.save(festa);
    }

    public Festa buscarPorId(Long id, Long empresaId) {
        return festaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Festa não encontrada"));
    }
}