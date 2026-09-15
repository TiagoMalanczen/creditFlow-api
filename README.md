# CreditFlow API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.x-blue.svg)](https://spring.io/projects/spring-data-jpa)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-red.svg)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.x-yellowgreen.svg)](https://site.mockito.org/)

Microsserviço financeiro desenvolvido para simulação, validação de regras de crédito e contratação de empréstimos consignados. O projeto foca em consistência transacional, rigor matemático e integridade de domínio sem o uso de geradores de código, aplicando arquitetura em camadas com Spring Boot e Java.

---

## Objetivos de Aprendizado & Destaques Técnicos

* **Cálculo Financeiro e Precisão Decimal**: Uso estrito de `BigDecimal` com arredondamento regulatório (`RoundingMode.HALF_EVEN`) para parcelamentos, taxas de juros e amortizações, eliminando falhas de representação binária de ponto flutuante.
* **Validação de Margem Consignável Acumulada**: Controle dinâmico de risco de crédito com validação em duas etapas:
1. Limite base de comprometimento fixado em no máximo 30% da renda mensal do usuário.
2. Consulta de contratos ativos (`APROVADO`) no banco para somar parcelas já comprometidas, garantindo que $\text{Parcelas Ativas} + \text{Nova Parcela} \le \text{Margem Total}$.


* **Imutabilidade e DTOs Modernos**: Uso de Java **Records** para blindar contratos de entrada e saída, desacoplando o modelo de persistência relacional do tráfego HTTP e aplicando validações com Jakarta Validation (`@NotNull`, `@Positive`).
* **Tratamento Global de Exceções**: Centralizado com `@RestControllerAdvice` na camada de infraestrutura, interceptando regras violadas (`MargemInsuficienteException`, `RecursoNaoEncontradoException`) e padronizando payloads de erro HTTP (`400`, `404`).
* **Cobertura de Testes de Unidade**: Testes automatizados isolados com **JUnit 5** e **Mockito** cobrindo cenários de borda, cálculos de juros e regras de margem acumulada na camada de serviço.

---

## Modelo de Dados

O banco de dados relacional (MySQL) é estruturado nas seguintes entidades centrais:

* **`usuario`**: Registro cadastral do solicitante, contendo nome, CPF, e-mail, senha criptografada, renda mensal comprovada (`BigDecimal`) e papel de autoridade (`Role`).
* **`emprestimo`**: Contrato de crédito associado ao usuário (`@ManyToOne`). Armazena valor solicitado, valor total com juros, número de parcelas, valor mensal da parcela, data de solicitação e ciclo de vida do status (`StatusEmprestimo`: `APROVADO`, `RECUSADO`, `QUITADO`).

---

## Tecnologias

* **Java 21**
* **Spring Boot 3.x**
* Spring Data JPA
* Spring Validation
* Spring Web


* **MySQL 8.0** / Hibernate
* **JUnit 5 & Mockito**
* **Lombok**
* **Maven**

---

## Estrutura do Projeto

```text
src/
├── main/java/com/creditflow/credit_api/
│   ├── config/             # Configurações de beans, segurança base e CORS
│   ├── controllers/        # Endpoints REST (Simulações, Contratação e Usuários)
│   ├── data/               # Entidades JPA (@Entity), Enums e Repositórios Spring Data
│   ├── dtos/               # Records de transporte para Requests e Responses
│   ├── exceptions/         # Exceções customizadas de negócio (Margem, NotFound)
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
| `GET` | `/v1/emprestimo/{id}` | Lista o histórico de empréstimos vinculados ao ID do usuário | `200 OK` |

## Roadmap e Próximos Passos

* [ ] Autenticação Stateless via **Spring Security 6** e tokens **JWT**.
* [ ] Controle e versionamento de schema com **Flyway Migrations**.
* [ ] Containerização de ambiente com **Docker & Docker Compose** (API + MySQL).
* [ ] Documentação interativa via **OpenAPI / Swagger UI**.
