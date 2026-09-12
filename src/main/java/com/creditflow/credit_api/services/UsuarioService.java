package com.creditflow.credit_api.services;

import com.creditflow.credit_api.data.enums.Role;
import com.creditflow.credit_api.data.model.UsuarioEntity;
import com.creditflow.credit_api.data.repositorys.UsuarioRepository;
import com.creditflow.credit_api.dtos.requests.CadastroUsuarioRequest;
import com.creditflow.credit_api.dtos.responses.UsuarioResponse;
import com.creditflow.credit_api.exceptions.DocumentoDuplicadoException;
import com.creditflow.credit_api.exceptions.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}


