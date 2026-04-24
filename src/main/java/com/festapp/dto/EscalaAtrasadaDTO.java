package com.festapp.dto;



public record EscalaAtrasadaDTO(
        Long id,
        String nomeCliente,
        String endereco,
        String dataHora,
        String funcionarioNome,
        String funcionarioEmail
) {}