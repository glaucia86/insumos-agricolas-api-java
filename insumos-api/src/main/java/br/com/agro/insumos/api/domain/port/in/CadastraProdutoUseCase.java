package br.com.agro.insumos.api.domain.port.in;

import br.com.agro.insumos.api.domain.model.Produto;
import java.math.BigDecimal;

public interface CadastraProdutoUseCase {
    Produto executar(String nome, String descricao, String categoria, String unidadeMedida, BigDecimal preco);
}