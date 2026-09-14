package com.creditflow.credit_api.controllers;

import com.creditflow.credit_api.dtos.requests.CadastroUsuarioRequest;
import com.creditflow.credit_api.dtos.responses.UsuarioResponse;
import com.creditflow.credit_api.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/usuarios")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrarCliente(@Valid @RequestBody  CadastroUsuarioRequest request){
        return usuarioService.cadastrarCliente(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UsuarioResponse buscarPorID(@Valid @PathVariable Long id){
        return usuarioService.buscarPorId(id);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<UsuarioResponse> listarTodos(){
        return usuarioService.listarTodosUsuario();
    }

    @PatchMapping("/{id}/renda")
    @ResponseStatus(HttpStatus.OK)
    public UsuarioResponse atualizarRenda(@Valid @RequestBody BigDecimal novaRenda,
                                          @PathVariable Long id){
        return usuarioService.atualizarRenda(id,novaRenda);
    }
}
