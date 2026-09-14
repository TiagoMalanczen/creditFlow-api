package com.creditflow.credit_api.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SimulacaoEmprestimoRequest(
        @Positive @NotNull BigDecimal valorSolicitado,
        @NotNull @Min(1) @Max(48) Integer numeroParcelas

) {
}
