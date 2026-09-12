package com.creditflow.credit_api.dtos.responses;

import com.creditflow.credit_api.data.enums.Role;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UsuarioResponse(
        Long id,
        String nomeCompleto,
        String email,
        BigDecimal rendaMensal,
        Role roles
) {
}
