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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Captor
    private ArgumentCaptor<EmprestimoEntity> emprestimoCaptor;

    private UsuarioEntity usuario;
    @BeforeEach
    void setUp(){
        usuario = new UsuarioEntity(1L,
                "Jair",
                "12454953",
                "jair@email.com",
                "123456",
                new BigDecimal("3000.00"),
                Role.ROLE_CLIENTE);
    }

    @Test
    @DisplayName("Emprestimo realizado com sucesso")
    public void deveAprovar(){

        SolicitacaoEmprestimoRequest solicitacao = new SolicitacaoEmprestimoRequest(
                1L,
                new BigDecimal("1000.00"),
                10
        );
       when(emprestimoRepository.findAllByUsuarioIdAndStatusEmprestimo(anyLong(), any())).thenReturn(List.of());
       when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
       when(emprestimoRepository.save(any(EmprestimoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        emprestimoService.solicitarEmprestimo(solicitacao);

        verify(emprestimoRepository).save(emprestimoCaptor.capture());
        EmprestimoEntity emprestimo = emprestimoCaptor.getValue();

        assertEquals(usuario, emprestimo.getUsuario());
        assertEquals(new BigDecimal("1100.00").setScale(2), emprestimo.getValorComJuros());
        assertEquals(new BigDecimal("110.00").setScale(2), emprestimo.getValorParcela());
        assertEquals(StatusEmprestimo.APROVADO, emprestimo.getStatusEmprestimo());
    }
    @Test
    @DisplayName("Emprestimo insuficiente por acumulo de parcelas")
    public void acumuloDeParcelas(){

        SolicitacaoEmprestimoRequest solicitacao = new SolicitacaoEmprestimoRequest(
                1L,
                new BigDecimal("3000.00"),
                10
        );
        EmprestimoEntity emprestimoAnterior =  EmprestimoEntity.builder()
                .valorParcela(new BigDecimal("660.00"))
                .build();

        when(emprestimoRepository.findAllByUsuarioIdAndStatusEmprestimo(1L, StatusEmprestimo.APROVADO)).thenReturn(List.of(emprestimoAnterior));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        MargemInsuficienteException exception = assertThrows(MargemInsuficienteException.class, () ->
                emprestimoService.solicitarEmprestimo(solicitacao));

        assertEquals("Margem insuficiente para emprestimo devido a acumulo de parcelas", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(emprestimoRepository,times(1)).findAllByUsuarioIdAndStatusEmprestimo(1L, StatusEmprestimo.APROVADO);
        verify(emprestimoRepository, never()).save(any(EmprestimoEntity.class));
    }
    @Test
    @DisplayName("Margem consignavel insuficiente")
    public void margemInsuficiente(){

        SolicitacaoEmprestimoRequest solicitacao = new SolicitacaoEmprestimoRequest(
                1L,
                new BigDecimal("10000.00"),
                5
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        MargemInsuficienteException exception = assertThrows(MargemInsuficienteException.class, () ->
                emprestimoService.solicitarEmprestimo(solicitacao));

        assertEquals("Margem insuficiente para emprestimo", exception.getMessage());

        verify(emprestimoRepository, never()).findAllByUsuarioIdAndStatusEmprestimo(any(), any());
        verify(emprestimoRepository, never()).save(any());
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

        verifyNoInteractions(emprestimoRepository);
        verify(usuarioRepository, times(1)).findById(100L);
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

    @ParameterizedTest
    @CsvSource({
            "1000.00, 1100.00, 220.00",
            "5000.00, 5500.00, 1100.00"
    })
    @DisplayName("Simulacao de emprestimo realizada com sucesso")
    public void simularEmprestimoSucesso(BigDecimal valorEnviado, BigDecimal valorEsperado, BigDecimal valorParcela){
        SimulacaoEmprestimoRequest request = new SimulacaoEmprestimoRequest(
                valorEnviado,
                5
        );
        SimulacaoEmprestimoResponse response  = emprestimoService.simularEmprestimo(request);

        assertEquals(valorEsperado, response.valorTotalComJuros());
        assertEquals(valorParcela, response.valorParcela());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emprestimoRepository);
    }

    @Test
    @DisplayName("Deve listar emprestimos com sucesso quando usuario existir")
    void deveListarEmprestimosComSucesso() {
        EmprestimoEntity emprestimo = EmprestimoEntity.builder()
                .id(10L)
                .usuario(usuario)
                .valorSolicitado(new BigDecimal("1000.00"))
                .valorParcela(new BigDecimal("110.00"))
                .valorComJuros(new BigDecimal("1100.00"))
                .numeroTotalParcelas(10)
                .dataSolicitacao(LocalDateTime.now())
                .statusEmprestimo(StatusEmprestimo.APROVADO)
                .build();

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(emprestimoRepository.findAllByUsuarioId(1L)).thenReturn(List.of(emprestimo));

        List<EmprestimoResponse> responses = emprestimoService.listarEmprestimoPorIdUsuario(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());

        EmprestimoResponse primeiroElemento = responses.get(0);
        assertEquals(emprestimo.getId(), primeiroElemento.id());
        assertEquals(emprestimo.getUsuario().getId(), primeiroElemento.usuarioId());
        assertEquals(emprestimo.getValorSolicitado(), primeiroElemento.valorSolicitado());
        assertEquals(emprestimo.getValorParcela(), primeiroElemento.valorParcela());
        assertEquals(emprestimo.getStatusEmprestimo(), primeiroElemento.status());

        verify(usuarioRepository, times(1)).existsById(1L);
        verify(emprestimoRepository, times(1)).findAllByUsuarioId(1L);
    }

}