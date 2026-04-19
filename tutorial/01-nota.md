# Etapa 2 — Arquitetura Hexagonal

> Este tutorial é voltado para desenvolvedoras TypeScript/Node.js que estão aprendendo Java com Spring Boot. Ao longo do caminho, cada conceito novo vem acompanhado de um paralelo com o mundo TypeScript que você já conhece.

---

## Índice

1. [O que é Arquitetura Hexagonal?](#1-o-que-é-arquitetura-hexagonal)
2. [A estrutura de pastas do projeto](#2-a-estrutura-de-pastas-do-projeto)
3. [Criando a estrutura no terminal](#3-criando-a-estrutura-no-terminal)
4. [Domínio — `Produto.java`](#4-domínio--produtojava)
5. [Port de Entrada — `CadastraProdutoUseCase.java`](#5-port-de-entrada--cadastraprodutousecasejava)
6. [Port de Saída — `ProdutoRepository.java`](#6-port-de-saída--produtorepositoryjava)
7. [Entendendo o fluxo completo](#7-entendendo-o-fluxo-completo)
8. [Domain Service — `CadastraProdutoService.java`](#8-domain-service--cadastraprodutoservicejava)
9. [Infraestrutura JPA — adicionando dependências](#9-infraestrutura-jpa--adicionando-dependências)
10. [Entidade JPA — `ProdutoEntity.java`](#10-entidade-jpa--produtoentityjava)
11. [Spring Data Repository — `ProdutoJpaRepository.java`](#11-spring-data-repository--produtojparepositoryjava)
12. [Adapter de Persistência — `ProdutoRepositoryAdapter.java`](#12-adapter-de-persistência--produtorepositoryadapterjava)

---

## 1. O que é Arquitetura Hexagonal?

No Node.js você provavelmente já usou uma estrutura em camadas assim:

```txt
src/
├── controllers/
├── services/
└── repositories/
```

A **Arquitetura Hexagonal** (também chamada de *Ports & Adapters*) evolui esse conceito com uma única regra central:

> [!TIP]
> **O domínio não conhece nada do mundo externo.** HTTP, banco de dados, Kafka — tudo isso é detalhe de infraestrutura.

O domínio fica no centro. O mundo externo se conecta a ele através de **ports** (interfaces) e **adapters** (implementações concretas).

### Analogia: o restaurante

Pensa no **chef** como o domínio — ele só sabe cozinhar. Não importa se o pedido veio do app, do WhatsApp ou do garçom, e não importa se os ingredientes vieram do mercado X ou Y. Ele só recebe a comanda e cozinha.

```
                    ┌─────────────────────────────┐
                    │                             │
  [App] ────────►  │         O CHEF              │ ────────►  [Geladeira]
  [Web] ────────►  │        (domínio)             │ ────────►  [Fornecedor]
  [Tel] ────────►  │                             │ ────────►  [Estoque]
                    │                             │
                    └─────────────────────────────┘

        ▲ ports/in                       ports/out ▲
    (quem entrega                     (o que o chef
      a comanda)                          precisa)
```

| Conceito | O que é | Paralelo TypeScript |
|---|---|---|
| `port/in` | Contrato do que o domínio aceita fazer | `interface ICreateProdutoUseCase` |
| `port/out` | Contrato do que o domínio precisa do mundo externo | `interface IProdutoRepository` |
| `domain/service` | Implementação da lógica de negócio | Classe `ProdutoService` |
| `infrastructure/web` | Adapter que traduz HTTP → domínio | Controller Express/Fastify |
| `infrastructure/persistence` | Adapter que traduz domínio → banco | Repository Prisma/TypeORM |

---

## 2. A estrutura de pastas do projeto

```txt
insumos-api/src/main/java/br/com/agro/insumos/api/
│
├── domain/                          # Núcleo — zero dependências externas
│   ├── model/                       # Entidades e Value Objects (Java puro)
│   ├── port/
│   │   ├── in/                      # Portas de entrada (contratos dos use cases)
│   │   └── out/                     # Portas de saída (contratos dos repositórios)
│   └── service/                     # Implementação dos use cases
│
├── application/                     # Orquestra o domínio
│   └── usecase/                     # Implementações concretas dos ports in
│
└── infrastructure/                  # Adapters — detalhes do mundo externo
    ├── web/                         # REST controllers (adapter in)
    │   └── dto/                     # Objetos de Request/Response
    ├── persistence/                 # JPA repositories (adapter out)
    │   └── entity/                  # Entidades JPA (mapeamento para o banco)
    └── config/                      # Configurações Spring
```

### Mapeamento com TypeScript

| Hexagonal Java | Equivalente TypeScript |
|---|---|
| `domain/model/` | Classes de domínio puras (sem decorators) |
| `port/in/` | Interface que o controller chama via DI |
| `port/out/` | Interface do repository que o service chama |
| `domain/service/` | Implementação do service |
| `infrastructure/web/` | Controllers Express/Fastify |
| `infrastructure/persistence/` | Repositories Prisma/TypeORM |
| `dto/` | Tipos de request/response (zod schemas, DTOs NestJS) |

---

## 3. Criando a estrutura no terminal

Dentro da pasta `insumos-api`, rode:

```bash
# Domínio
mkdir -p src/main/java/br/com/agro/insumos/api/domain/model
mkdir -p src/main/java/br/com/agro/insumos/api/domain/port/in
mkdir -p src/main/java/br/com/agro/insumos/api/domain/port/out
mkdir -p src/main/java/br/com/agro/insumos/api/domain/service

# Application
mkdir -p src/main/java/br/com/agro/insumos/api/application/usecase

# Infrastructure
mkdir -p src/main/java/br/com/agro/insumos/api/infrastructure/web/dto
mkdir -p src/main/java/br/com/agro/insumos/api/infrastructure/persistence/entity
mkdir -p src/main/java/br/com/agro/insumos/api/infrastructure/config
```

Confirme que tudo foi criado:

```bash
find src/main/java/br/com/agro/insumos/api -type d
```

> [!NOTE]
> No Windows (PowerShell), substitua `mkdir -p` por `New-Item -ItemType Directory -Force -Path`.

---

## 4. Domínio — `Produto.java`

Crie `src/main/java/br/com/agro/insumos/api/domain/model/Produto.java`:

```java
package br.com.agro.insumos.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Produto {

    private UUID id;
    private String nome;
    private String descricao;
    private String categoria;
    private String unidadeMedida;
    private BigDecimal preco;
    private Boolean ativo;

    public Produto(UUID id, String nome, String descricao, String categoria, String unidadeMedida, BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.unidadeMedida = unidadeMedida;
        this.preco = preco;
        this.ativo = true;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getCategoria() { return categoria; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public BigDecimal getPreco() { return preco; }
    public Boolean getAtivo() { return ativo; }
}
```

> [!NOTE]
> **Por que sem Lombok aqui?**
> O modelo de domínio é Java puro — sem anotações de framework. Isso garante que o domínio não tem dependências externas. Lombok vai entrar nos DTOs e nas entidades JPA, onde faz sentido.

> [!NOTE]
> **Por que `BigDecimal` e não `double`?**
> Para valores monetários, `double` tem problemas de precisão em ponto flutuante. `BigDecimal` é o padrão em qualquer código Java sério que lida com dinheiro ou preços.

---

## 5. Port de Entrada — `CadastraProdutoUseCase.java`

Crie `src/main/java/br/com/agro/insumos/api/domain/port/in/CadastraProdutoUseCase.java`:

```java
package br.com.agro.insumos.api.domain.port.in;

import br.com.agro.insumos.api.domain.model.Produto;
import java.math.BigDecimal;

public interface CadastraProdutoUseCase {
    Produto executar(String nome, String descricao, String categoria, String unidadeMedida, BigDecimal preco);
}
```

Esta é a **comanda do chef** — o contrato do que o domínio aceita fazer. O controller REST vai depender dessa interface, nunca da implementação direta.

O controller REST vai preencher essa comanda. Amanhã, se chegar um consumer Kafka, ele também preenche a mesma comanda. **O chef não muda — só quem entrega a comanda muda.**

> [!NOTE]
> **Paralelo TypeScript:**
> ```typescript
> interface ICadastraProdutoUseCase {
>   executar(nome: string, descricao: string, categoria: string,
>            unidadeMedida: string, preco: number): Promise<Produto>
> }
> ```
> É exatamente como você definiria uma interface que o controller recebe via injeção de dependência no Inversify ou tsyringe.

---

## 6. Port de Saída — `ProdutoRepository.java`

Crie `src/main/java/br/com/agro/insumos/api/domain/port/out/ProdutoRepository.java`:

```java
package br.com.agro.insumos.api.domain.port.out;

import br.com.agro.insumos.api.domain.model.Produto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository {
    Produto salvar(Produto produto);
    Optional<Produto> buscarPorId(UUID id);
    List<Produto> listarTodos();
}
```

Esta é a **lista de ingredientes do chef** — o domínio define o que precisa do banco, mas não sabe se os dados vêm do PostgreSQL, MongoDB ou de um arquivo txt.

> [!NOTE]
> **Por que `Optional<Produto>` e não `Produto`?**
> `Optional<T>` é o equivalente Java de `T | null` no TypeScript. Ele força quem chama a tratar explicitamente o caso em que o produto não existe, em vez de receber um `null` silencioso.

> [!IMPORTANT]
> **Por que essa interface está em `domain/port/out` e não em `infrastructure`?**
> O domínio **define o contrato** que o banco precisa cumprir — não o contrário. A implementação JPA vai ficar em `infrastructure/persistence` e vai *implementar* essa interface.

---

## 7. Entendendo o fluxo completo

Antes de criar o service, vale visualizar como tudo se conecta:

```
[HTTP Request]
     │
     ▼
[Controller REST]          ← adapter in   (traduz HTTP → comanda)
     │
     │  chama
     ▼
[CadastraProdutoUseCase]   ← port in      (a comanda — interface)
     │
     │  implementado por
     ▼
[CadastraProdutoService]   ← domain service (o chef — lógica de negócio)
     │
     │  chama
     ▼
[ProdutoRepository]        ← port out     (lista de ingredientes — interface)
     │
     │  implementado por
     ▼
[ProdutoRepositoryAdapter] ← adapter out  (busca no PostgreSQL de verdade)
     │
     ▼
[PostgreSQL]
```

### Por que esse design importa num projeto real?

Squads grandes conseguem trocar peças sem quebrar o resto:

| Cenário | O que muda | O que não toca |
|---|---|---|
| Migrar do Oracle para PostgreSQL | Só o adapter out | Domínio intacto |
| Adicionar endpoint gRPC além do REST | Só cria novo adapter in | Domínio intacto |
| Testar o service sem banco | Cria um adapter out fake em memória | Domínio intacto |

> **O domínio nunca muda por causa de infraestrutura. Esse é o valor central da arquitetura.**

---

## 8. Domain Service — `CadastraProdutoService.java`

Crie `src/main/java/br/com/agro/insumos/api/domain/service/CadastraProdutoService.java`:

```java
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
```

O que está acontecendo:

- **`implements CadastraProdutoUseCase`** — é o chef recebendo a comanda
- **Depende de `ProdutoRepository`** — mas só da interface, não da implementação JPA
- **Gera o `UUID` no domínio** — a identidade do produto é responsabilidade do domínio, não do banco
- **Zero anotações Spring** — Java puro, testável sem subir nenhum contexto de aplicação

> [!NOTE]
> **Paralelo TypeScript:**
> ```typescript
> class CadastraProdutoService implements ICadastraProdutoUseCase {
>   constructor(private readonly repository: IProdutoRepository) {}
>
>   async executar(dto: CadastraProdutoDto): Promise<Produto> {
>     const produto = new Produto(randomUUID(), dto.nome, ...)
>     return this.repository.salvar(produto)
>   }
> }
> ```

---

## 9. Infraestrutura JPA — adicionando dependências

Antes de criar os arquivos de persistência, adicione as dependências de banco ao `pom.xml`.

Localize o bloco `<dependencies>` e adicione:

```xml
<!-- Spring Data JPA — equivalente ao TypeORM/Prisma -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Driver PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Salve e resolva as dependências:

```bash
./mvnw dependency:resolve
```

Saída esperada no final:

```txt
BUILD SUCCESS
```

---

## 10. Entidade JPA — `ProdutoEntity.java`

Crie `src/main/java/br/com/agro/insumos/api/infrastructure/persistence/entity/ProdutoEntity.java`:

```java
package br.com.agro.insumos.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

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
```

> [!NOTE]
> **Por que uma entidade separada do model de domínio?**
> O `Produto.java` do domínio é Java puro — sem anotações de framework. A `ProdutoEntity` é o mapeamento para o banco — responsabilidade exclusiva da infraestrutura. Se um dia a equipe migrar de JPA para outra solução, o domínio não toca.
>
> **Paralelo TypeScript:** é como separar sua classe de domínio `Produto` da sua entidade TypeORM `ProdutoEntity` com `@Entity()`, `@Column()` etc.

**Anotações Lombok usadas aqui:**

| Anotação | O que faz | Equivalente TypeScript |
|---|---|---|
| `@Data` | Gera getters, setters, `equals`, `hashCode` e `toString` | Campos públicos com `get`/`set` automáticos |
| `@NoArgsConstructor` | Construtor sem argumentos (JPA exige) | `constructor() {}` |
| `@AllArgsConstructor` | Construtor com todos os campos | `constructor(id, nome, ...)` |

---

## 11. Spring Data Repository — `ProdutoJpaRepository.java`

Crie `src/main/java/br/com/agro/insumos/api/infrastructure/persistence/ProdutoJpaRepository.java`:

```java
package br.com.agro.insumos.api.infrastructure.persistence;

import br.com.agro.insumos.api.infrastructure.persistence.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, UUID> {
}
```

Só de estender `JpaRepository` você ganha `save`, `findById`, `findAll`, `delete` e muito mais — sem escrever uma linha de SQL.

> [!NOTE]
> **Paralelo TypeScript:**
> É o equivalente ao `Repository<ProdutoEntity>` do TypeORM que você injeta e já tem todos os métodos CRUD disponíveis — ou ao `prisma.produto` com os métodos `create`, `findUnique`, `findMany` etc.

---

## 12. Adapter de Persistência — `ProdutoRepositoryAdapter.java`

Crie `src/main/java/br/com/agro/insumos/api/infrastructure/persistence/ProdutoRepositoryAdapter.java`:

```java
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
    public List<Produto> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    // --- Mappers ---

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
```

Esta é a peça que conecta o domínio ao banco. Ela implementa `ProdutoRepository` (interface do domínio) usando o `ProdutoJpaRepository` (Spring Data). O domínio nunca sabe que existe JPA.

| Elemento | Papel |
|---|---|
| `toEntity` / `toDomain` | Mappers que traduzem entre domínio e banco |
| `@Component` | Registra a classe no container Spring (equivalente ao `@Injectable()` do NestJS) |
| `@RequiredArgsConstructor` | Lombok gera o construtor com `jpaRepository` automaticamente |

> [!NOTE]
> **Paralelo TypeScript com `@RequiredArgsConstructor`:**
> ```typescript
> @Injectable()
> class ProdutoRepositoryAdapter implements IProdutoRepository {
>   constructor(private readonly jpaRepository: ProdutoJpaRepository) {}
> }
> ```

---

## Resumo da etapa

Você construiu toda a espinha dorsal da arquitetura hexagonal:

```
domain/model/Produto.java               ← entidade de domínio (Java puro)
domain/port/in/CadastraProdutoUseCase   ← contrato de entrada
domain/port/out/ProdutoRepository       ← contrato de saída
domain/service/CadastraProdutoService   ← lógica de negócio
infrastructure/persistence/entity/      ← entidade JPA
infrastructure/persistence/             ← adapter que fala com o banco
```

Na próxima etapa, criaremos o **controller REST** e os **DTOs** para expor a API via HTTP.






