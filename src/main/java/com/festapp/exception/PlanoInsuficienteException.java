package com.festapp.exception;

import com.festapp.model.enums.PlanoTipo;

public class PlanoInsuficienteException extends RuntimeException {
    public PlanoInsuficienteException(PlanoTipo planoNecessario) {
        super("Funcionalidade disponível a partir do plano " + planoNecessario.name());
    }
}
