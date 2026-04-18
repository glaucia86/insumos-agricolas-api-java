# Insumos Agrícolas API — Java 21 + Spring Boot + Arquitetura Hexagonal + Maven

## Motivação

API de Gestão de Insumos Agrícolas em Java 21 + Spring Boot + Arquitetura Hexagonal é um projeto de estudo para aprender Java e Spring Boot, focado em práticas e convenções corporativas. 

O objetivo é criar uma API RESTful para gerenciar insumos agrícolas, utilizando as melhores práticas de desenvolvimento em Java, como o uso de Lombok para reduzir boilerplate, Maven Wrapper para consistência de builds e a estrutura típica de um projeto Spring Boot.

O intuito, além de aprender Java e Spring Boot, é entender as práticas, convenções e ferramentas comuns em ambientes corporativos, como o uso de Lombok para reduzir boilerplate, Maven Wrapper para consistência de builds e a estrutura típica de um projeto Spring Boot.

Como a minha experiência prévia é principalmente com TypeScript e Node.js, este projeto também serve para mapear conceitos e práticas entre os dois ecossistemas, facilitando a transição e o entendimento das diferenças e semelhanças.

---

## Stack

| Tecnologia | Versão | Equivalente TS/Node.js |
|---|---|---|
| Java (Temurin) | 21 LTS | Node.js runtime |
| Maven | 3.9.15 | npm |
| Spring Boot | 3.5.13 | Express/Fastify + ecosystem |
| Lombok | latest | N/A (Java precisa, TS não) |

---

## Etapa 1 — Setup do Ambiente ✅

### O que foi implementado

- JDK 21 (Eclipse Temurin) instalado e configurado no Windows
- Maven 3.9.15 instalado e configurado no PATH do sistema
- VSCode configurado com extensões Java, Spring Boot e SonarLint
- Projeto base gerado via Spring Initializr
- Maven Wrapper (`mvnw`) configurado — padrão em pipelines CI/CD corporativos
- Build validado com `./mvnw clean package -DskipTests`
- Repositório GitHub inicializado com `.gitignore` adequado para Java/Maven

### Decisões técnicas

- **JDK 21 LTS**: versão adotada por grandes corporações; traz Records,
  Pattern Matching e Virtual Threads
- **Eclipse Temurin**: distribuição OpenJDK mantida pela Eclipse Foundation,
  padrão em ambientes AWS/ECS corporativos
- **Spring Boot 3.5.x**: versão estável mais recente da linha 3.x;
  squads corporativos levam tempo para migrar para versões major
- **Lombok**: elimina boilerplate de getters/setters/construtores;
  onipresente no ecossistema Java corporativo
- **Maven Wrapper**: garante que todos os ambientes (local e CI/CD)
  usem exatamente a mesma versão do Maven, sem dependência de instalação global

### Mapeamento TypeScript → Java

| TypeScript/Node.js | Java |
|---|---|
| `node` (runtime) | `JVM` |
| `tsc` (compilador) | `javac` |
| `package.json` | `pom.xml` |
| `node_modules/` | `~/.m2/repository/` |
| `npm run build` | `./mvnw package` |
| `npm test` | `./mvnw test` |
| `npx` | `./mvnw` (Maven Wrapper) |

### Como rodar localmente

```bash
cd insumos-api
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

### Pendente para a próxima etapa

- Estruturar o projeto com Arquitetura Hexagonal (ports & adapters)
- Criar o primeiro domínio: `Produto`
- Implementar o primeiro endpoint REST: `POST /produtos`
- Conectar ao PostgreSQL via Spring Data JPA

---

## Pré-requisitos para desenvolvimento

- JDK 21 (Eclipse Temurin)
- Maven 3.9.x (ou usar o `./mvnw` incluso no projeto)
- VSCode com extensões: `vscjava.vscode-java-pack`, `vmware.vscode-spring-boot`