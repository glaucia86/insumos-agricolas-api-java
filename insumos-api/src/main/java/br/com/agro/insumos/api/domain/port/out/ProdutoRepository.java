package br.com.agro.insumos.api.domain.port.out;

import br.com.agro.insumos.api.domain.model.Produto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository {
    Produto salvar(Produto produto);
    Optional<Produto> buscarPorId(UUID id);
    List<Produto> buscarTodos();
}
