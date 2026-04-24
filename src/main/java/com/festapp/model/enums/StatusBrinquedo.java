package com.festapp.model;

/**
 * Define os estados permitidos para um brinquedo.
 * O uso de Enum previne que estados inválidos sejam injetados via API.
 */
public enum StatusBrinquedo {
    DISPONIVEL,
    RESERVADO,
    MANUTENCAO
}