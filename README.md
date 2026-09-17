# CreditFlow API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-blue.svg)](https://spring.io/projects/spring-data-jpa)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-red.svg)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.x-yellowgreen.svg)](https://site.mockito.org/)
[![Flyway](https://img.shields.io/badge/Flyway-12.4.0-red.svg)](https://flywaydb.org/)

> 🚧 **Em desenvolvimento.** Cadastro de usuários, simulação e contratação de crédito consignado já funcionam. Autenticação via Spring Security/JWT ainda está em implementação.

Microsserviço financeiro desenvolvido para simulação, validação de regras de crédito e contratação de empréstimos consignados. O projeto foca em consistência transacional, rigor matemático e integridade de domínio, com código escrito manualmente (sem geração assistida por IA), aplicando arquitetura em camadas com Spring Boot e Java.

---

## Objetivos de Aprendizado & Destaques Técnicos

* **Cálculo Financeiro e Precisão Decimal**: Uso estrito de `BigDecimal` com arredondamento bancário (`RoundingMode.HALF_EVEN`) para juros e parcelamentos, eliminando falhas de representação binária de ponto flutuante. A taxa de juros aplicada atualmente é fixa (10% sobre o valor solicitado, diluída no número de parcelas) — uma tabela de taxas por prazo/perfil está no roadmap.
* **Validação de Margem Consignável Acumulada**: Controle dinâmico de risco de crédito com validação em duas etapas:
  1. Limite base de comprometimento fixado em até 30% da renda mensal do usuário (mais conservador que o teto legal de 35% do consignado tradicional).
  2. Consulta de contratos ativos (`APROVADO`) no banco para somar parcelas já comprometidas, garantindo que $\text{Parcelas Ativas} + \text{Nova Parcela} \le \text{Margem Total}$.
* **Imutabilidade e DTOs Modernos**: Uso de Java **Records** para blindar contratos de entrada e saída, desacoplando o modelo de persistência relacional do tráfego HTTP e aplicando validações com Jakarta Validation (`@NotNull`, `@Positive`).
* **Tratamento Global de Exceções**: Centralizado com `@RestControllerAdvice`, interceptando regras violadas (`MargemInsuficienteException`, `RecursoNaoEncontradoException`, `DocumentoDuplicadoException`), erros de validação de campo (`MethodArgumentNotValidException`) e acesso negado (`AccessDeniedException`), com payloads de erro HTTP padronizados.
* **Cobertura de Testes de Unidade**: Testes automatizados isolados com **JUnit 5** e **Mockito** cobrindo cenários de sucesso, borda e erro na camada de serviço — aprovação, acúmulo de margem, margem insuficiente, usuário inexistente, dados nulos, simulação e listagem.
* **Versionamento de Schema com Flyway**: Controle de evolução do banco de dados relacional por meio de migrações SQL versionadas e imutáveis. O ciclo de vida do schema é acoplado à aplicação com `baseline-on-migrate` para compatibilidade com bases existentes e `ddl-auto: validate` no Hibernate, impedindo alterações destrutivas automáticas em tempo de execução.
---

## Modelo de Dados

O banco de dados relacional (MySQL) é estruturado nas seguintes entidades centrais:

* **`usuario`**: Registro cadastral do solicitante, contendo nome, CPF, e-mail, senha criptografada com BCrypt, renda mensal comprovada (`BigDecimal`) e papel de autoridade (`Role`). Implementa `UserDetails` do Spring Security para uso futuro na autenticação.
* **`emprestimos`**: Contrato de crédito associado ao usuário (`@ManyToOne`). Armazena valor solicitado, valor total com juros, número de parcelas, valor mensal da parcela, data de solicitação e ciclo de vida do status (`StatusEmprestimo`: `APROVADO`, `RECUSADO`, `QUITADO`).

---

## Tecnologias

* **Java 21**
* **Spring Boot 4.1.1**
* Spring Data JPA
* Spring Security (BCrypt já em uso; autenticação via JWT em implementação)
* Spring Validation
* Spring Web
* **Flyway 12.4.0** (Database Migrations)

* **MySQL 8.0** / Hibernate
* **JUnit 5 & Mockito**
* **Lombok**
* **Maven**

---

## Estrutura do Projeto

```text
src/
├── main/java/com/creditflow/credit_api/
│   ├── config/             # Configurações de beans, segurança e FlywayConfig
│   ├── controllers/        # Endpoints REST (Simulações, Contratação e Usuários)
│   ├── data/               # Entidades JPA (@Entity), Enums e Repositórios Spring Data
│   ├── dtos/               # Records de transporte para Requests e Responses
│   ├── exceptions/         # Exceções customizadas de negócio (Margem, NotFound, Documento Duplicado)
│   ├── infra/              # @RestControllerAdvice e padronização de erros HTTP
│   ├── services/           # Regras financeiras, cálculos de margem e orquestração
│   └── CreditApiApplication.java
│
└── test/java/com/creditflow/credit_api/
    ├── services/           # Testes unitários com Mockito isolando a regra de negócio
    │   ├── EmprestimoServiceTest.java
    │   └── UsuarioServiceTest.java
    └── CreditApiApplicationTests.java
```

## Como Rodar o Projeto

### Pré-requisitos
* JDK 21
* MySQL 8.0 rodando localmente (ou acessível via rede)

### Variáveis de ambiente
```bash
export DATABASE_USERNAME=seu_usuario
export DATABASE_PASSWORD=sua_senha
```
O schema `credit-api` é criado automaticamente na primeira execução (`createDatabaseIfNotExist=true`).

### Executando
```bash
./mvnw spring-boot:run
```
A API sobe em `http://localhost:8080`.

### Rodando os testes
```bash
./mvnw test
```

---

## Endpoints da API

### Usuários (`/v1/usuarios`)

| Método | Rota | Descrição | Status Sucesso |
| --- | --- | --- | --- |
| `POST` | `/v1/usuarios` | Cadastra um novo cliente com renda mensal | `201 Created` |
| `GET` | `/v1/usuarios` | Lista todos os usuários cadastrados | `200 OK` |
| `GET` | `/v1/usuarios/{id}` | Busca os dados cadastrais de um usuário por ID | `200 OK` |
| `PATCH` | `/v1/usuarios/{id}/renda` | Atualiza a renda mensal comprovada do usuário | `200 OK` |

### Empréstimos (`/v1/emprestimo`)

| Método | Rota | Descrição | Status Sucesso |
| --- | --- | --- | --- |
| `POST` | `/v1/emprestimo/simulacao` | Simula valores, juros e parcelas sem persistência | `200 OK` |
| `POST` | `/v1/emprestimo/solicitacao` | Valida margem ativa e efetiva a contratação do crédito | `201 Created` |
| `GET` | `/v1/emprestimo/{id}` | Lista o histórico de empréstimos vinculados ao **ID do usuário** informado | `200 OK` |

> ⚠️ Nota: `GET /v1/emprestimo/{id}` recebe o ID do **usuário**, não do empréstimo. Uma revisão para `/v1/usuarios/{id}/emprestimos` está prevista para deixar a rota mais alinhada com convenções REST.

---

## Segurança — Status Atual

Toda a API está com `permitAll()` liberado enquanto a autenticação não é finalizada (ver Roadmap). O que já está implementado:
* Senhas armazenadas com **BCrypt** (`PasswordEncoder`).
* `UsuarioEntity` já implementa `UserDetails`, preparado para integração com Spring Security.
* Sessão configurada como **stateless** (`SessionCreationPolicy.STATELESS`), antecipando autenticação via token.

O que falta: filtro de autenticação JWT, regras de autorização por rota/role, e proteção efetiva dos endpoints.

---

## Roadmap e Próximos Passos

* [ ] Autenticação Stateless via **Spring Security** e tokens **JWT**.
* [ ] Containerização de ambiente com **Docker & Docker Compose** (API + MySQL).
* [ ] Documentação interativa via **OpenAPI / Swagger UI**.
* [ ] Tabela de taxas de juros por prazo/perfil, substituindo a taxa fixa atual.
