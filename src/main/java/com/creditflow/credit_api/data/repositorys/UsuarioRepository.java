package com.creditflow.credit_api.data.repositorys;

import com.creditflow.credit_api.data.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);
    boolean existsByCpf(String Cpf);
    boolean existsByEmail(String email);
    Optional<UsuarioEntity> findById(Long id);
}
