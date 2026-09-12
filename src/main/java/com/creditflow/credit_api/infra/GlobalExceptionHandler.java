package com.creditflow.credit_api.infra;

import com.creditflow.credit_api.dtos.ErroResponseDto;
import com.creditflow.credit_api.exceptions.DocumentoDuplicadoException;
import com.creditflow.credit_api.exceptions.MargemInsuficienteException;
import com.creditflow.credit_api.exceptions.RecursoNaoEncontradoException;
import com.creditflow.credit_api.exceptions.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ResponseEntity<ErroResponseDto> handlerDocumentoDuplicadoException(DocumentoDuplicadoException e, HttpServletRequest request){
        ErroResponseDto erro = new ErroResponseDto(
            LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                e.getMessage(),
                "Documentos duplicados",
                request.getRequestURI(),
                Map.of()

        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MargemInsuficienteException.class)
    public ResponseEntity<ErroResponseDto> handlerMargemInsuficienteException(MargemInsuficienteException e, HttpServletRequest request){
        ErroResponseDto erro = new ErroResponseDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "Margem Insuficiente",
                request.getRequestURI(),
                Map.of()

        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDto> handlerRecursoNaoEncontradoException(RecursoNaoEncontradoException e, HttpServletRequest request){
        ErroResponseDto erro = new ErroResponseDto(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                "Recurso nao encontrado",
                request.getRequestURI(),
                Map.of()

        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDto> handlerRegraNegocioException(RegraNegocioException e, HttpServletRequest request){
        ErroResponseDto erro = new ErroResponseDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "Regra de negocio violada",
                request.getRequestURI(),
                Map.of()

        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDto> handlerMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request){

        Map<String, String> erros = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            erros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErroResponseDto erro = new ErroResponseDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validacao nos campos informados",
                "Erro de validacao",
                request.getRequestURI(),
                erros
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDto> handlerAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ErroResponseDto erro = new ErroResponseDto(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Você não tem permissão para acessar este recurso.",
                request.getRequestURI(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }
}
