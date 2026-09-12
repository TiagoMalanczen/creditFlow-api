package com.creditflow.credit_api.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponseDto(
        LocalDateTime timestamp,
        Integer status,
        String erro,
        String mensagem,
        String path,
        Map<String , String> errosValidacao
) {
}
