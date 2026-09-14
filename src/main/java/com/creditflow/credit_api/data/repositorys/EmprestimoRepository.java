package com.creditflow.credit_api.data.repositorys;

import com.creditflow.credit_api.data.enums.StatusEmprestimo;
import com.creditflow.credit_api.data.model.EmprestimoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<EmprestimoEntity, Long> {

    List<EmprestimoEntity> findAllByUsuarioId(Long usuarioId);
    List<EmprestimoEntity> findAllByUsuarioIdAndStatusEmprestimo(Long usuarioId, StatusEmprestimo status);

}
