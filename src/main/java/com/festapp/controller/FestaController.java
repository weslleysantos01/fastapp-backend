package com.festapp.controller;

import com.festapp.model.Escala;
import com.festapp.model.Festa;
import com.festapp.service.EscalaService;
import com.festapp.service.FestaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/festas")
@CrossOrigin(origins = "http://localhost:3000")
public class FestaController {

    @Autowired
    private FestaService festaService;

    @Autowired
    private EscalaService escalaService;

    @GetMapping
    public List<Festa> listar() {
        return festaService.listarTodas();
    }

    @PostMapping
    public Festa cadastrar(@RequestBody Festa festa) {
        return festaService.cadastrar(festa);
    }

    @PostMapping("/{id}/alocar")
    public List<Escala> alocar(@PathVariable Long id) {
        return escalaService.alocarEquipe(id);
    }
}