package com.creditflow.credit_api.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank @Email String email,
        @NotBlank String senha
){
}
