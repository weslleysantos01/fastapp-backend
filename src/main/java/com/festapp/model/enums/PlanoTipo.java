package com.festapp.model.enums;

public enum PlanoTipo {
    BASICO,
    PROFISSIONAL,
    PREMIUM;

    public boolean temAcesso(PlanoTipo planoRequerido) {
        return this.ordinal() >= planoRequerido.ordinal();
    }
}
