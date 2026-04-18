package br.com.agro.insumos.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Produto {
    private UUID id;
    private String nome;
    private String descricao;
    private String unidadeMedida;
    private BigDecimal preco;
    private Boolean ativo;

    public Produto(UUID id, String nome, String descricao, String unidadeMedida, BigDecimal preco, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida;
        this.preco = preco;
        this.ativo = true;
    }

    // Getters
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public BigDecimal getPreco() { return preco; }
    public Boolean getAtivo() { return ativo; }
}
