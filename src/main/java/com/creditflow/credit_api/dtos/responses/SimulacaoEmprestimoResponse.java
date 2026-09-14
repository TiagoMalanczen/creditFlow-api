package com.creditflow.credit_api.dtos.responses;

import com.creditflow.credit_api.data.enums.StatusEmprestimo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SimulacaoEmprestimoResponse(
        BigDecimal valorSolicitado,
        Integer numeroParcelas,
        BigDecimal valorParcela,
        BigDecimal valorTotalComJuros
) {
}
