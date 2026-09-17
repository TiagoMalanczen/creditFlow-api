package com.creditflow.credit_api.data.model;


import com.creditflow.credit_api.data.enums.StatusEmprestimo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Integer numeroTotalParcelas;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEmprestimo statusEmprestimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;
}
