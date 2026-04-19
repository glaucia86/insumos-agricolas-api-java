package br.com.agro.insumos.api.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agro.insumos.api.infrastructure.persistence.entity.ProdutoEntity;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, UUID> { }
