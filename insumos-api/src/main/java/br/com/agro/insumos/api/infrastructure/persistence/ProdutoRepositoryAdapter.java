package br.com.agro.insumos.api.infrastructure.persistence;

import br.com.agro.insumos.api.domain.model.Produto;
import br.com.agro.insumos.api.domain.port.out.ProdutoRepository;
import br.com.agro.insumos.api.infrastructure.persistence.entity.ProdutoEntity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProdutoRepositoryAdapter implements ProdutoRepository {

    private final ProdutoJpaRepository jpaRepository;

    @Override
    public Produto salvar(Produto produto) {
        ProdutoEntity entity = Objects.requireNonNull(toEntity(produto));
        ProdutoEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Produto> buscarPorId(@NonNull UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Produto> buscarTodos() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private ProdutoEntity toEntity(Produto produto) {
        return new ProdutoEntity(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getUnidadeMedida(),
                produto.getPreco(),
                produto.getAtivo()
        );
    }

    private Produto toDomain(ProdutoEntity entity) {
        return new Produto(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getCategoria(),
                entity.getUnidadeMedida(),
                entity.getPreco()
        );
    }
}