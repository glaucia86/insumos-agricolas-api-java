# Etapa 2 — Arquitetura Hexagonal

Antes de escrever qualquer código, precisa entender a estrutura que vamos montar — porque ela define onde cada arquivo vai viver pelo resto do projeto.

---

## O que é Arquitetura Hexagonal?

No Node.js você provavelmente já usou uma estrutura em camadas parecida com isso:

```txt
src/
├── controllers/
├── services/
└── repositories/
```

A Arquitetura Hexagonal (Ports & Adapters) evolui esse conceito com uma regra central:

> **O domínio não conhece nada do mundo externo.** HTTP, banco de dados, Kafka — tudo isso é detalhe de infraestrutura.

O domínio fica no centro. O mundo externo se conecta a ele através de **ports** (interfaces) e **adapters** (implementações).

---

## A estrutura que vamos adotar

```txt
insumos-api/src/main/java/br/com/agro/insumos/api/
│
├── domain/                          # Núcleo — zero dependências externas
│   ├── model/                       # Entidades e Value Objects
│   ├── port/
│   │   ├── in/                      # Portas de entrada (use cases)
│   │   └── out/                     # Portas de saída (repositórios, eventos)
│   └── service/                     # Implementação dos use cases
│
├── application/                     # Orquestra o domínio
│   └── usecase/                     # Implementação concreta dos ports in
│
└── infrastructure/                  # Adapters — detalhes do mundo externo
    ├── web/                         # REST controllers (adapter in)
    │   └── dto/                     # Request/Response bodies
    ├── persistence/                 # JPA repositories (adapter out)
    │   └── entity/                  # Entidades JPA (mapeamento banco)
    └── config/                      # Configurações Spring
```

---

## Mapeamento com TypeScript

| Hexagonal | TypeScript equivalente |
|---|---|
| `domain/model/` | Suas classes de domínio puras |
| `port/in/` | Interface do service que o controller chama |
| `port/out/` | Interface do repository que o service chama |
| `domain/service/` | Implementação do service |
| `infrastructure/web/` | Controllers Express/Fastify |
| `infrastructure/persistence/` | Repositories Prisma/TypeORM |
| `dto/` | Tipos de request/response (zod schemas) |

---

## Vamos criar a estrutura

No terminal, dentro de `insumos-api`:

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

Confirme que criou com:

```bash
find src/main/java/br/com/agro/insumos/api -type d
```

## ✅ Estrutura hexagonal criada!

Agora vamos criar o primeiro domínio. Vamos de fora para dentro — começando pelo modelo de domínio.

---

## Primeiro arquivo: `Produto.java`

Crie o arquivo `src/main/java/br/com/agro/insumos/api/domain/model/Produto.java`:

<details><summary><b>Produto.java</b></summary>
<br/>

```java
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

    public Produto(UUID id, String nome, String descricao, String unidadeMedida, BigDecimal preco) {
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
```

</details>
<br/>

> **Por que sem Lombok aqui?** O modelo de domínio é puro Java — sem anotações de framework. Isso garante que o domínio não tem dependências externas. Lombok vai entrar nos DTOs e nas entidades JPA.

> **Por que `BigDecimal` e não `double`?** Para valores monetários e preços no contexto financeiro/agro, `double` tem problemas de precisão. `BigDecimal` é o padrão em contexto bancário — você vai ver isso em todo código Java sério que lida com dinheiro.

---

Crie o arquivo e me confirme. Vamos criar a **porta de entrada** na sequência. 🚀

## Port de Entrada — `CadastraProdutoUseCase.java`

Crie `src/main/java/br/com/agro/insumos/api/domain/port/in/CadastraProdutoUseCase.java`:

```java
package br.com.agro.insumos.api.domain.port.in;

import br.com.agro.insumos.api.domain.model.Produto;

public interface CadastraProdutoUseCase {
    Produto executar(String nome, String descricao, String unidadeMedida, java.math.BigDecimal preco);
}
```

> **O que é isso no modelo hexagonal?** É a **porta de entrada** — define o contrato do que o domínio aceita fazer. O controller REST vai depender dessa interface, nunca da implementação direta.

> **Paralelo TypeScript:** é exatamente como você definiria uma `interface ICreateProdutoUseCase` que o controller recebe via injeção de dependência no Inversify/tsyringe.

---

## Port de Saída — `ProdutoRepository.java`

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

> **Por que `Optional`?** É o equivalente ao `T | null` do TypeScript — força quem chama a tratar explicitamente o caso em que o produto não existe, em vez de receber um `null` silencioso.

> **Por que essa interface está no `domain/port/out` e não em `infrastructure`?** Porque o domínio **define o contrato** que o banco precisa cumprir — não o contrário. A implementação JPA vai ficar em `infrastructure/persistence` e vai implementar essa interface.

---

## Explicação detalhada sobre Arquitetura Hexagonal

# Arquitetura Hexagonal — A Explicação Dummie

Vou usar uma analogia do mundo real antes de voltar para o código.

---

## Pensa no domínio como um restaurante

O **chef** (domínio) só sabe cozinhar. Ele não sabe se o pedido veio por telefone, app, ou balcão. Ele não sabe se os ingredientes vieram do mercado X ou Y. Ele só recebe um pedido e cozinha.

```
                    ┌─────────────────────────────┐
                    │                             │
  [App] ────────►  │         O CHEF              │ ────────►  [Geladeira]
  [Web] ────────►  │        (domínio)             │ ────────►  [Fornecedor]
  [Tel] ────────►  │                             │ ────────►  [Estoque]
                    │                             │
                    └─────────────────────────────┘

        ▲ ports/in                    ports/out ▲
     (quem faz                           (o que o
      o pedido)                        chef precisa)
```

---

## `port/in` — A comanda que chega ao chef

É o **contrato do que o chef aceita fazer**.

Não importa se o pedido veio do app, do WhatsApp ou do garçom — todos preenchem a mesma comanda:

```txt
COMANDA:
- Nome do prato: ___________
- Observações: ___________
- Quantidade: ___________
```

No código isso é:

```java
// port/in/CadastraProdutoUseCase.java
public interface CadastraProdutoUseCase {
    Produto executar(String nome, String descricao, ...);
}
```

O controller REST vai preencher essa comanda. Amanhã se vier um consumer Kafka, ele também preenche a mesma comanda. **O chef não muda — só quem entrega a comanda muda.**

---

## `port/out` — O que o chef precisa do mundo externo

O chef precisa de ingredientes — mas não vai ao mercado buscar. Ele **define o que precisa** e alguém traz:

```txt
LISTA DO CHEF:
- Quero salvar um produto
- Quero buscar um produto por id
- Quero listar todos
```

No código:

```java
// port/out/ProdutoRepository.java
public interface ProdutoRepository {
    Produto salvar(Produto produto);
    Optional<Produto> buscarPorId(UUID id);
    List<Produto> listarTodos();
}
```

O chef não sabe se os ingredientes vêm do PostgreSQL, do MongoDB ou de um arquivo txt. Isso é problema de quem implementa a lista — não do chef.

---

## O fluxo completo

```
[HTTP Request]
     │
     ▼
[Controller REST]          ← adapter in  (traduz HTTP → comanda)
     │
     │  chama
     ▼
[CadastraProdutoUseCase]   ← port in     (a comanda — interface)
     │
     │  implementado por
     ▼
[CadastraProdutoService]   ← domain service (o chef — lógica de negócio)
     │
     │  chama
     ▼
[ProdutoRepository]        ← port out    (a lista de ingredientes — interface)
     │
     │  implementado por
     ▼
[ProdutoRepositoryJpa]     ← adapter out (busca no PostgreSQL de verdade)
     │
     ▼
[PostgreSQL]
```

---

## Por que isso importa num projeto real?

Squads grandes trocam peças sem quebrar o resto:

- Migraram do Oracle para o PostgreSQL? Só troca o **adapter out** — o domínio não toca.
- Adicionaram um endpoint gRPC além do REST? Só cria um novo **adapter in** — o domínio não toca.
- Precisam testar o service sem banco? Criam um **adapter out fake em memória** — o domínio não toca.

**O domínio nunca muda por causa de infraestrutura.** Esse é o valor central da arquitetura.

---

Na sequência criaremos o **service** que implementa o use case. 🚀
