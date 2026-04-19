package br.com.agro.insumos.api.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "produtos")
public class ProdutoEntity {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column
    private String descricao;

    @Column
    private String categoria;

    @Column(name = "unidade_medida", nullable = false)
    private String unidadeMedida;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal preco;

    @Column(nullable = false)
    private Boolean ativo;
}
