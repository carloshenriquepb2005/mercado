package br.com.mercado.precificacao.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {
    @Id
    @Column(name = "codigo")
    private Long codigo;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "perfil", nullable = false)
    private Integer perfil;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(Long codigo, String nome, String senha, Integer perfil) {
        this.codigo = codigo;
        this.nome = nome;
        this.senha = senha;
        this.perfil = perfil;
    }

    public Long getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public Integer getPerfil() { return perfil; }
}
