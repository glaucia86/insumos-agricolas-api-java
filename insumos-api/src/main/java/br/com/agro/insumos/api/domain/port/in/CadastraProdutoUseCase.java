package br.com.agro.insumos.api.domain.port.in;

import br.com.agro.insumos.api.domain.model.Produto;

public interface CadastraProdutoUseCase {
    Produto exectutar(String nome, String descricao, String categoria, String unidadeMedida, java.math.BigDecimal preco);
}
