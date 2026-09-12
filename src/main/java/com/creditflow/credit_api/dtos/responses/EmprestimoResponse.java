package com.creditflow.credit_api.dtos.responses;

import com.creditflow.credit_api.data.enums.StatusEmprestimo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EmprestimoResponse(
        Long id,
        Long usuarioId,
        BigDecimal valorSolicitado,
        Integer numeroParcelas,
        BigDecimal valorParcela,
        BigDecimal valorTotalComJuros,
        StatusEmprestimo status,
        LocalDateTime dataSolicitacao
) {
}
