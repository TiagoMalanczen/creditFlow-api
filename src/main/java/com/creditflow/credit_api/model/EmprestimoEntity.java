package com.creditflow.credit_api.model;


import com.creditflow.credit_api.enums.StatusEmprestimo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emprestimos")
public class EmprestimoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorSolicitado;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorParcela;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal valorComJuros;

    @Column(nullable = false)
    private Integer numeroParcelas;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEmprestimo statusEmprestimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;
}
