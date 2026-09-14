package com.creditflow.credit_api.services;

import com.creditflow.credit_api.data.enums.Role;
import com.creditflow.credit_api.data.model.UsuarioEntity;
import com.creditflow.credit_api.data.repositorys.UsuarioRepository;
import com.creditflow.credit_api.dtos.requests.CadastroUsuarioRequest;
import com.creditflow.credit_api.dtos.responses.UsuarioResponse;
import com.creditflow.credit_api.exceptions.DocumentoDuplicadoException;
import com.creditflow.credit_api.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.creditflow.credit_api.exceptions.RegraNegocioException;

import java.math.BigDecimal;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Cadastro realizado com sucesso")
    public void cadastroRealizado(){
        CadastroUsuarioRequest cadastro = new CadastroUsuarioRequest(
                "Maicon",
                "12456",
                "maicon@gmail.com",
                "senha123",
                new BigDecimal("1500.0")
        );

       when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
       when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
       when(passwordEncoder.encode("senha123")).thenReturn("hashBcryptSeguro");
       when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(inv -> {
                    UsuarioEntity u = inv.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        UsuarioResponse response = usuarioService.cadastrarCliente(cadastro);
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Maicon", response.nomeCompleto());
        assertEquals("maicon@gmail.com", response.email());


        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));


       verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
    }
    @Test
    @DisplayName("Cadastro com email duplicado")
    public void emailDuplicado(){
        CadastroUsuarioRequest cadastro = new CadastroUsuarioRequest(
                "Maicon",
                "12456",
                "maicon@gmail.com",
                "senha123",
                new BigDecimal("1500.0")
        );
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        DocumentoDuplicadoException exception = assertThrows(DocumentoDuplicadoException.class, () ->
                usuarioService.cadastrarCliente(cadastro));

        assertEquals("E-mail ja cadastrado no sistema", exception.getMessage());

        verify(usuarioRepository, never()).save(any());
    }
    @Test
    @DisplayName("Cadastro com cpf duplicado")
    public void cpfDuplicado(){
        CadastroUsuarioRequest cadastro = new CadastroUsuarioRequest(
                "Maicon",
                "12456",
                "maicon@gmail.com",
                "senha123",
                new BigDecimal("1500.0")
        );
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(true);

        DocumentoDuplicadoException exception = assertThrows(DocumentoDuplicadoException.class, () ->
                usuarioService.cadastrarCliente(cadastro));

        assertEquals("CPF ja cadastrado no sistema", exception.getMessage());

        verify(usuarioRepository, never()).save(any());
    }
    @Test
    @DisplayName("Cadastro com dados nulos")
    public void dadosNulos(){
        CadastroUsuarioRequest cadastro = null;

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () ->
                usuarioService.cadastrarCliente(cadastro));

        assertEquals("Dados da solicitacao nao podem ser nulos", exception.getMessage());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Buscar por id sucesso")
    public void buscarId(){
        UsuarioEntity usuario = UsuarioEntity.builder()
                .id(1L)
                .nomeCompleto("Pedro")
                .cpf("12456789")
                .senha("asdga")
                .email("pedro@gmail.com")
                .role(Role.ROLE_CLIENTE)
                .rendaMensal(new BigDecimal("1500.0"))
                .build();

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        UsuarioResponse response =  usuarioService.buscarPorId(usuario.getId());

        assertEquals(usuario.getId(), response.id());
        assertNotNull(response);
        assertEquals(usuario.getNomeCompleto(), response.nomeCompleto());
        assertEquals(usuario.getEmail(), response.email());

        verify(usuarioRepository, times(1)).findById(usuario.getId());
    }
    @Test
    @DisplayName("Busca por id nao encontrada no banco")
    public void buscaPorIdNaoEncontrada(){

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () ->
                usuarioService.buscarPorId(99L));

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Sucesso em atualizar renda")
    public void atulizarRendaSucesso(){
        UsuarioEntity usuario = UsuarioEntity.builder()
                .id(1L)
                .nomeCompleto("Pedro")
                .cpf("12456789")
                .senha("asdga")
                .email("pedro@gmail.com")
                .role(Role.ROLE_CLIENTE)
                .rendaMensal(new BigDecimal("1500.0"))
                .build();

        BigDecimal novaRenda = new BigDecimal("1500.0") ;

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.atualizarRenda(usuario.getId(), novaRenda);

        assertEquals(novaRenda, response.rendaMensal());

        verify(usuarioRepository, times(1)).findById(usuario.getId());
        verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));

    }
    @Test
    @DisplayName("Falha em atualizar a renda do usuario")
    public void falhaAtualizarRenda(){
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () ->
                usuarioService.atualizarRenda(99L, new BigDecimal("5000.0")));

        assertEquals("Usuario nao encontrado", exception.getMessage());

        verify(usuarioRepository, never()).save(any());
    }

}