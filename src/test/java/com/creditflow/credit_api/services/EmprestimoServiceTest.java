package com.creditflow.credit_api.services;

import com.creditflow.credit_api.data.enums.Role;
import com.creditflow.credit_api.data.enums.StatusEmprestimo;
import com.creditflow.credit_api.data.model.EmprestimoEntity;
import com.creditflow.credit_api.data.model.UsuarioEntity;
import com.creditflow.credit_api.data.repositorys.EmprestimoRepository;
import com.creditflow.credit_api.data.repositorys.UsuarioRepository;
import com.creditflow.credit_api.dtos.requests.SolicitacaoEmprestimoRequest;
import com.creditflow.credit_api.dtos.responses.EmprestimoResponse;
import com.creditflow.credit_api.exceptions.MargemInsuficienteException;
import com.creditflow.credit_api.exceptions.RecursoNaoEncontradoException;
import com.creditflow.credit_api.exceptions.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmprestimoService emprestimoService;


    @Test
    @DisplayName("Emprestimo realizado com sucesso")
    public void deveAprovar(){

        UsuarioEntity usuario = new UsuarioEntity(1L,
                "Jair",
                "12454953",
                "jair@email.com",
                "123456",
                new BigDecimal("3000.0"),
                Role.ROLE_CLIENTE);
        SolicitacaoEmprestimoRequest solicitacao = new SolicitacaoEmprestimoRequest(
                1L,
                new BigDecimal("1000.00"),
                10
        );

       when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
       when(emprestimoRepository.save(any(EmprestimoEntity.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        EmprestimoResponse response = emprestimoService.solicitarEmprestimo(solicitacao);

        assertEquals(BigDecimal.valueOf(1100.00).setScale(2), response.valorTotalComJuros());
        assertEquals(BigDecimal.valueOf(110.0).setScale(2), response.valorParcela());
        assertEquals(StatusEmprestimo.APROVADO, response.status());

        verify(emprestimoRepository, times(1)).save(any(EmprestimoEntity.class));
    }


    @Test
    @DisplayName("Usuario nao encontrado")
    public void usuarioNaoEncontrado(){
        when(usuarioRepository.findById(100L)).thenReturn(Optional.empty());

        SolicitacaoEmprestimoRequest solicitacao = new SolicitacaoEmprestimoRequest(
                100L,
                new BigDecimal("1000.00"),
                10
        );

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () ->
                emprestimoService.solicitarEmprestimo(solicitacao));

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(emprestimoRepository, never()).save(any());
    }


    @Test
    @DisplayName("Margem consignavel insuficiente")
    public void margemInsuficiente(){
        UsuarioEntity usuario = new UsuarioEntity(1L,
                "Jair",
                "12454953",
                "jair@email.com",
                "123456",
                new BigDecimal("2000.0"),
                Role.ROLE_CLIENTE);
        SolicitacaoEmprestimoRequest solicitacao = new SolicitacaoEmprestimoRequest(
                1L,
                new BigDecimal("10000.00"),
                5
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        MargemInsuficienteException exception = assertThrows(MargemInsuficienteException.class, () ->
                emprestimoService.solicitarEmprestimo(solicitacao));

        assertEquals("Margem insuficiente para emprestimo", exception.getMessage());

        verify(emprestimoRepository, never()).save(any());

    }

    @Test
    @DisplayName("Chamadas nulas")
    public void chamadasNulas(){
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () ->
                emprestimoService.solicitarEmprestimo(null));

        assertEquals("Dados da solicitacao nao podem ser nulos", exception.getMessage());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emprestimoRepository);
    }


}