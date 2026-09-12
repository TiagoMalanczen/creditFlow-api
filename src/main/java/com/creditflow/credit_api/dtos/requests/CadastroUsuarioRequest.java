package com.creditflow.credit_api.dtos.requests;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CadastroUsuarioRequest(
        @NotBlank String nomeCompleto,
        @NotBlank String cpf,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String senha,
        @NotNull @Positive BigDecimal rendaMensal
        ) {
}
