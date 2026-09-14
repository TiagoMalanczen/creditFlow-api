package com.creditflow.credit_api.controllers;

import com.creditflow.credit_api.dtos.requests.SimulacaoEmprestimoRequest;
import com.creditflow.credit_api.dtos.requests.SolicitacaoEmprestimoRequest;
import com.creditflow.credit_api.dtos.responses.EmprestimoResponse;
import com.creditflow.credit_api.dtos.responses.SimulacaoEmprestimoResponse;
import com.creditflow.credit_api.services.EmprestimoService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("v1/emprestimo")

public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping("/solicitacao")
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResponse solicitarEmprestimo(@Valid @RequestBody SolicitacaoEmprestimoRequest request){
        return emprestimoService.solicitarEmprestimo(request);
    }

    @PostMapping("/simulacao")
    @ResponseStatus(HttpStatus.OK)
    public SimulacaoEmprestimoResponse simularEmprestimo(@Valid @RequestBody SimulacaoEmprestimoRequest request){
        return emprestimoService.simularEmprestimo(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<EmprestimoResponse> lsitarEmprestimosPorId(@Valid @PathVariable Long id){
        return emprestimoService.listarEmprestimoPorIdUsuario(id);
    }

}
