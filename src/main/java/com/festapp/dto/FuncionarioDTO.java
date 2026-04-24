package com.festapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para cadastro de funcionário — evita mass assignment.
 * Somente campos permitidos são expostos.
 */
@Data
public class FuncionarioDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email
    private String email;

    @Size(max = 20)
    private String telefone;

    @NotBlank(message = "Sexo é obrigatório")
    private String sexo;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    @Size(max = 100)
    private String tipoBarraca;

    @NotNull(message = "Empresa não informada")
    private Long empresaId;
}
