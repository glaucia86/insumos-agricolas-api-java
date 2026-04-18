package br.com.agro.insumos.api.domain.service;

import br.com.agro.insumos.api.domain.model.Produto;
import br.com.agro.insumos.api.domain.port.in.CadastraProdutoUseCase;
import br.com.agro.insumos.api.domain.port.out.ProdutoRepository;
import java.math.BigDecimal;
import java.util.UUID;

public class CadastraProdutoService implements CadastraProdutoUseCase {

    private final ProdutoRepository produtoRepository;

    public CadastraProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Produto executar(String nome, String descricao, String categoria, String unidadeMedida, BigDecimal preco) {
        Produto produto = new Produto(
            UUID.randomUUID(),
            nome,
            descricao,
            categoria,
            unidadeMedida,
            preco
        );
        return produtoRepository.salvar(produto);
    }
}