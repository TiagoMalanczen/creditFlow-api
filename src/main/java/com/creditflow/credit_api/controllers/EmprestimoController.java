package com.creditflow.credit_api.controllers;

import com.creditflow.credit_api.dtos.requests.SolicitacaoEmprestimoRequest;
import com.creditflow.credit_api.dtos.responses.EmprestimoResponse;
import com.creditflow.credit_api.services.EmprestimoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Valid
@RequestMapping("v1/emprestimo")

public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public EmprestimoResponse solicitarEmprestimo(@Valid @RequestBody SolicitacaoEmprestimoRequest request){
        return emprestimoService.solicitarEmprestimo(request);
    }


}
