package com.creditflow.credit_api.services;

import com.creditflow.credit_api.data.enums.Role;
import com.creditflow.credit_api.data.model.UsuarioEntity;
import com.creditflow.credit_api.data.repositorys.UsuarioRepository;
import com.creditflow.credit_api.dtos.requests.CadastroUsuarioRequest;
import com.creditflow.credit_api.dtos.responses.UsuarioResponse;
import com.creditflow.credit_api.exceptions.DocumentoDuplicadoException;
import com.creditflow.credit_api.exceptions.RecursoNaoEncontradoException;
import com.creditflow.credit_api.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<UsuarioEntity> usuarioCaptor;

    private CadastroUsuarioRequest cadastro;
    private UsuarioEntity usuarioPadrao;

    @BeforeEach
    void setUp() {
        cadastro = new CadastroUsuarioRequest(
                "Maicon",
                "12456",
                "maicon@gmail.com",
                "senha123",
                new BigDecimal("1500.00")
        );

        usuarioPadrao = UsuarioEntity.builder()
                .id(1L)
                .nomeCompleto("Pedro")
                .cpf("12456789")
                .senha("hashBcryptSeguro")
                .email("pedro@gmail.com")
                .role(Role.ROLE_CLIENTE)
                .rendaMensal(new BigDecimal("1500.00"))
                .build();
    }

    @Test
    @DisplayName("Cadastro realizado com sucesso")
    void cadastroRealizado() {
        when(usuarioRepository.existsByEmail("maicon@gmail.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12456")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashBcryptSeguro");
        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(inv -> {
                    UsuarioEntity u = inv.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        UsuarioResponse response = usuarioService.cadastrarCliente(cadastro);

        verify(usuarioRepository).save(usuarioCaptor.capture());
        UsuarioEntity usuarioSalvo = usuarioCaptor.getValue();

        assertEquals("Maicon", usuarioSalvo.getNomeCompleto());
        assertEquals("12456", usuarioSalvo.getCpf());
        assertEquals("maicon@gmail.com", usuarioSalvo.getEmail());
        assertEquals("hashBcryptSeguro", usuarioSalvo.getSenha());
        assertEquals(Role.ROLE_CLIENTE, usuarioSalvo.getRole());
        assertEquals(new BigDecimal("1500.00"), usuarioSalvo.getRendaMensal());

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Maicon", response.nomeCompleto());
        assertEquals("maicon@gmail.com", response.email());

        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Cadastro com email duplicado")
    void emailDuplicado() {
        when(usuarioRepository.existsByEmail("maicon@gmail.com")).thenReturn(true);

        DocumentoDuplicadoException exception = assertThrows(DocumentoDuplicadoException.class, () ->
                usuarioService.cadastrarCliente(cadastro));

        assertEquals("E-mail ja cadastrado no sistema", exception.getMessage());

        verify(usuarioRepository, never()).existsByCpf(anyString());
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastro com cpf duplicado")
    void cpfDuplicado() {
        when(usuarioRepository.existsByEmail("maicon@gmail.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12456")).thenReturn(true);

        DocumentoDuplicadoException exception = assertThrows(DocumentoDuplicadoException.class, () ->
                usuarioService.cadastrarCliente(cadastro));

        assertEquals("CPF ja cadastrado no sistema", exception.getMessage());

        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastro com dados nulos")
    void dadosNulos() {
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () ->
                usuarioService.cadastrarCliente(null));

        assertEquals("Dados da solicitacao nao podem ser nulos", exception.getMessage());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Buscar por id sucesso")
    void buscarId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPadrao));

        UsuarioResponse response = usuarioService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(usuarioPadrao.getId(), response.id());
        assertEquals(usuarioPadrao.getNomeCompleto(), response.nomeCompleto());
        assertEquals(usuarioPadrao.getEmail(), response.email());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Busca por id nao encontrada no banco")
    void buscaPorIdNaoEncontrada() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () ->
                usuarioService.buscarPorId(99L));

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Sucesso em atualizar renda")
    void atulizarRendaSucesso() {
        BigDecimal novaRenda = new BigDecimal("3500.00");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPadrao));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.atualizarRenda(1L, novaRenda);

        verify(usuarioRepository).save(usuarioCaptor.capture());
        UsuarioEntity usuarioSalvo = usuarioCaptor.getValue();

        assertEquals(novaRenda, usuarioSalvo.getRendaMensal());
        assertNotNull(response);
        assertEquals(novaRenda, response.rendaMensal());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Falha em atualizar a renda do usuario")
    void falhaAtualizarRenda() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () ->
                usuarioService.atualizarRenda(99L, new BigDecimal("5000.00")));

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(99L);
        verify(usuarioRepository, never()).save(any());
    }
}