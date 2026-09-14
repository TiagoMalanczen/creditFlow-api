package com.creditflow.credit_api.services;

import com.creditflow.credit_api.data.enums.Role;
import com.creditflow.credit_api.data.enums.StatusEmprestimo;
import com.creditflow.credit_api.data.model.EmprestimoEntity;
import com.creditflow.credit_api.data.model.UsuarioEntity;
import com.creditflow.credit_api.data.repositorys.EmprestimoRepository;
import com.creditflow.credit_api.data.repositorys.UsuarioRepository;
import com.creditflow.credit_api.dtos.requests.SimulacaoEmprestimoRequest;
import com.creditflow.credit_api.dtos.requests.SolicitacaoEmprestimoRequest;
import com.creditflow.credit_api.dtos.responses.EmprestimoResponse;
import com.creditflow.credit_api.dtos.responses.SimulacaoEmprestimoResponse;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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


    @Test
    @DisplayName("Simulacao de emprestimo realizada com sucesso")
    public void simularEmprestimoSucesso(){
        SimulacaoEmprestimoRequest request = new SimulacaoEmprestimoRequest(
                new BigDecimal("5000.0"),
                5
        );
        SimulacaoEmprestimoResponse response  = emprestimoService.simularEmprestimo(request);

        assertEquals(new BigDecimal("1100.00"), response.valorParcela());
        assertEquals(new BigDecimal("5500.00"), response.valorTotalComJuros());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emprestimoRepository);
    }

    @Test
    @DisplayName("Listar emprestimos com sucesso")
    public void listarEmprestimosSucesso(){
        UsuarioEntity usuario = new UsuarioEntity(1L,
                "Jair",
                "12454953",
                "jair@email.com",
                "123456",
                new BigDecimal("3000.0"),
                Role.ROLE_CLIENTE);
        EmprestimoEntity emprestimo = new EmprestimoEntity(
                1L,
                new BigDecimal("1000"),
                new BigDecimal("110"),
                new BigDecimal("1110"),
                10,
                LocalDateTime.now(),
                StatusEmprestimo.APROVADO,
                usuario
        );

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(emprestimoRepository.findAllByUsuarioId(1L)).thenReturn(List.of(emprestimo));

        List<EmprestimoResponse> responses = emprestimoService.listarEmprestimoPorIdUsuario(usuario.getId());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(emprestimo.getId(), responses.get(0).id());
        assertEquals(emprestimo.getValorSolicitado(), responses.get(0).valorSolicitado());


        verify(usuarioRepository,times(1)).existsById(1L);
        verify(emprestimoRepository, times(1)).findAllByUsuarioId(1L);
    }
    @Test
    @DisplayName("Emprestimos nao encotrados")
    public void listarEmprestimosErro(){

        when(usuarioRepository.existsById(1L)).thenReturn(false);

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () ->
                emprestimoService.listarEmprestimoPorIdUsuario(1L));

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).existsById(1L);
        verifyNoInteractions(emprestimoRepository);
    }

}