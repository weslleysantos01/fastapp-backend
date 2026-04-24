package com.festapp.aspect;

import com.festapp.annotation.RequerPlano;
import com.festapp.exception.PlanoInsuficienteException;
import com.festapp.model.enums.PlanoTipo;
import com.festapp.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PlanoAspect {

    private final EmpresaRepository empresaRepository;

    @Before("@annotation(requerPlano)")
    public void verificarPlano(RequerPlano requerPlano) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        PlanoTipo planoAtual = empresaRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"))
                .getPlano();

        if (!planoAtual.temAcesso(requerPlano.valor())) {
            throw new PlanoInsuficienteException(requerPlano.valor());
        }
    }
}
