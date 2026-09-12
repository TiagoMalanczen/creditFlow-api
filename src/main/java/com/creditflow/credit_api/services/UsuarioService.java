package com.creditflow.credit_api.services;

import com.creditflow.credit_api.data.enums.Role;
import com.creditflow.credit_api.data.model.UsuarioEntity;
import com.creditflow.credit_api.data.repositorys.UsuarioRepository;
import com.creditflow.credit_api.dtos.requests.CadastroUsuarioRequest;
import com.creditflow.credit_api.dtos.responses.UsuarioResponse;
import com.creditflow.credit_api.exceptions.DocumentoDuplicadoException;
import com.creditflow.credit_api.exceptions.RecursoNaoEncontradoException;
import com.creditflow.credit_api.exceptions.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrarCliente(CadastroUsuarioRequest request){
        if (request == null) {
            throw new RegraNegocioException("Dados da solicitacao nao podem ser nulos");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DocumentoDuplicadoException("E-mail ja cadastrado no sistema");
        }
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new DocumentoDuplicadoException("CPF ja cadastrado no sistema");
        }
        UsuarioEntity usuarioSalvo = usuarioRepository.save(UsuarioEntity.builder()
                        .nomeCompleto(request.nomeCompleto())
                        .cpf(request.cpf())
                        .email(request.email())
                        .senha(passwordEncoder.encode(request.senha()))
                        .rendaMensal(request.rendaMensal())
                        .role(Role.ROLE_CLIENTE)
                .build());

        return  new UsuarioResponse(
                usuarioSalvo.getId(),
                usuarioSalvo.getNomeCompleto(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getRendaMensal(),
                usuarioSalvo.getRole());
    }

    public UsuarioResponse buscarPorId(Long idUsuario){
        if(idUsuario == null){
            throw new RegraNegocioException("Dados nulos");
        }
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        return new UsuarioResponse(usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getRendaMensal(),
                 usuario.getRole());

    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodosUsuario(){
        List<UsuarioEntity> listaUsuarios = usuarioRepository.findAll();

        List<UsuarioResponse> lista = listaUsuarios
                .stream()
                .map(usuario -> new UsuarioResponse(
                        usuario.getId(),
                        usuario.getNomeCompleto(),
                        usuario.getEmail(),
                        usuario.getRendaMensal(),
                        usuario.getRole()
                ))
                .toList();

        return lista;
    }

    @Transactional
    public UsuarioResponse atualizarRenda(Long idUsuario, BigDecimal novaRenda){

        if (idUsuario == null || novaRenda == null || novaRenda.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Dados invalidos para atualizacao de renda");
        }
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        usuario.setRendaMensal(novaRenda);
        usuarioRepository.save(usuario);

        return new UsuarioResponse(usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getRendaMensal(),
                usuario.getRole());
    }
}


