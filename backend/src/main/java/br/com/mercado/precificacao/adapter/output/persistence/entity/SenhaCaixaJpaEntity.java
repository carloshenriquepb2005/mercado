package br.com.mercado.precificacao.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "senha")
public class SenhaCaixaJpaEntity {
    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "SENHA_CAIXA", nullable = false, length = 10)
    private String senhaCaixa;

    protected SenhaCaixaJpaEntity() {
    }
}
