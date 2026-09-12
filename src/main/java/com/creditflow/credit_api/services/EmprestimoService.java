package com.creditflow.credit_api.services;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EmprestimoResponse solicitarEmprestimo(SolicitacaoEmprestimoRequest request){
        if (request == null) {
            throw new RegraNegocioException("Dados da solicitacao nao podem ser nulos");
        }

        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        BigDecimal juros = BigDecimal.valueOf(1.10)
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal valorComJuros = request.valorSolicitado()
                .multiply(juros).setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal valorParcela = valorComJuros.divide(
                BigDecimal.valueOf(request.numeroParcelas()),
                2,
                RoundingMode.HALF_EVEN);

        BigDecimal margemDisponivel = usuario.getRendaMensal()
                .multiply(BigDecimal.valueOf(0.30))
                .setScale(2, RoundingMode.HALF_EVEN);

        if(valorParcela.compareTo(margemDisponivel) > 0){
            throw  new MargemInsuficienteException("Margem insuficiente para emprsetimo");
        }

        EmprestimoEntity emprestimo = (EmprestimoEntity.builder()
                        .valorSolicitado(request.valorSolicitado())
                        .valorParcela(valorParcela)
                        .valorComJuros(valorComJuros)
                        .numeroParcelas(request.numeroParcelas())
                        .dataSolicitacao(LocalDateTime.now())
                        .statusEmprestimo(StatusEmprestimo.APROVADO)
                        .usuario(usuario)
                .build());
        EmprestimoEntity emprestimoSalvo = emprestimoRepository.save(emprestimo);

        return new EmprestimoResponse(
                emprestimoSalvo.getId(),
                emprestimoSalvo.getUsuario().getId(),
                request.valorSolicitado(),
                request.numeroParcelas(),
                valorParcela,
                valorComJuros,
                emprestimoSalvo.getStatusEmprestimo(),
                emprestimoSalvo.getDataSolicitacao()
        );
    }
}
