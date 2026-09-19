# Sistema de Entrega – API REST

API REST para gerenciamento de um sistema de entregas (delivery), desenvolvida com **Java 21** e **Spring Boot**, seguindo arquitetura em camadas (Controller → Service → Repository), com persistência em **MySQL**.

## Sumário
- [Arquitetura e tecnologias](#arquitetura-e-tecnologias)
- [Modelo de domínio](#modelo-de-domínio)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do banco de dados](#configuração-do-banco-de-dados)
- [Como executar](#como-executar)
- [Autenticação](#autenticação)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Coleção de requisições (Postman/Insomnia)](#coleção-de-requisições-postmaninsomnia)
- [Regras de negócio implementadas](#regras-de-negócio-implementadas)
- [Testes automatizados](#testes-automatizados)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Roteiro sugerido para apresentação/demonstração](#roteiro-sugerido-para-apresentaçãodemonstração)

## Arquitetura e tecnologias

| Item | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.x |
| Gerenciador de dependências | Maven |
| Banco de dados | MySQL (H2 em memória para os testes) |
| Persistência | Spring Data JPA / Hibernate |
| Segurança | Spring Security + JWT |
| Validação | Bean Validation (Jakarta Validation) |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5 + Mockito + AssertJ |

A aplicação segue uma **arquitetura em camadas**:

```
controller  → recebe requisições HTTP, valida entrada (DTOs) e delega ao service
service     → contém as regras de negócio e orquestra os repositórios
repository  → acesso a dados via Spring Data JPA
model       → entidades JPA mapeadas para o banco de dados
dto         → objetos de requisição/resposta, dissociados das entidades
mapper      → conversão entre entidades e DTOs
exception   → exceções de domínio + tratamento centralizado (@RestControllerAdvice)
security    → autenticação/autorização via JWT
config      → configurações de segurança, OpenAPI e carga inicial de dados
```

## Modelo de domínio

Entidades e relacionamentos:

- **Cliente** 1:1 **Endereco** (endereço de entrega padrão do cliente)
- **Cliente** 1:N **Pedido** (um cliente pode ter vários pedidos)
- **Entregador** 1:N **Pedido** (um entregador atende vários pedidos ao longo do tempo; associação opcional)
- **Categoria** 1:N **Produto** (uma categoria agrupa vários produtos)
- **Pedido** 1:N **ItemPedido** (um pedido possui vários itens)
- **ItemPedido** N:1 **Produto** (cada item referencia um produto)
- **Usuario** (conta de acesso à API, com papel `ADMIN` ou `OPERADOR`)

## Pré-requisitos

- JDK 21+
- Maven 3.9+ (ou utilize o wrapper `./mvnw` incluído no projeto)
- MySQL 8+ em execução localmente (ou acessível via rede)

## Configuração do banco de dados

1. Crie um usuário/senha no MySQL com permissão para criar bancos (ou utilize `root`).
2. As credenciais podem ser informadas via variáveis de ambiente (recomendado) ou diretamente em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sistema_entrega?createDatabaseIfNotExist=true&useTimezone=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
```

O banco `sistema_entrega` é criado automaticamente na primeira conexão (`createDatabaseIfNotExist=true`), e as tabelas são geradas/atualizadas automaticamente pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

Exemplo de variáveis de ambiente (Linux/macOS):

```bash
export DB_USERNAME=root
export DB_PASSWORD=sua_senha
```

## Como executar

```bash
# 1. Clonar o repositório
git clone <url-do-repositorio>
cd api

# 2. Compilar e rodar os testes
./mvnw clean test

# 3. Executar a aplicação
./mvnw spring-boot:run
```

A API sobe por padrão em `http://localhost:8080`.

Na primeira execução, um usuário administrador padrão é criado automaticamente (ver `DataSeeder`):

```
username: admin
password: admin123
role: ADMIN
```

> Altere essa senha assim que possível em um ambiente real; ela existe apenas para facilitar os primeiros testes.

## Autenticação

A API utiliza **JWT** (Bearer Token). O fluxo é:

1. `POST /api/auth/login` (ou `/api/auth/registrar` para criar um novo usuário) retorna um token.
2. Envie esse token no cabeçalho `Authorization: Bearer <token>` nas demais requisições.

Exemplo de login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Resposta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

Controle de acesso: qualquer usuário autenticado (`ADMIN` ou `OPERADOR`) pode consultar e criar/atualizar registros; operações de **exclusão** (`DELETE`) exigem o papel `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).

## Documentação da API (Swagger)

Com a aplicação em execução, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Use o botão **Authorize** no Swagger UI para informar o token JWT obtido no login e testar os endpoints protegidos.

## Coleção de requisições (Postman/Insomnia)

O arquivo [`postman/sistema-entrega.postman_collection.json`](postman/sistema-entrega.postman_collection.json) contém uma coleção pronta com todos os principais endpoints (autenticação, clientes, entregadores, categorias, produtos e pedidos), incluindo um fluxo de exemplo do início ao fim (login → cadastro de cliente/produto → criação de pedido → atribuição de entregador → atualização de status).

Basta importar o arquivo no Postman (ou Insomnia) e configurar a variável `baseUrl` (padrão `http://localhost:8080`). A variável `token` é preenchida automaticamente após a requisição de login, através de um script de teste da própria coleção.

## Regras de negócio implementadas

- **Controle de estoque**: ao criar um pedido, o estoque de cada produto é validado e baixado automaticamente; se não houver estoque suficiente, o pedido é rejeitado.
- **Cancelamento/rejeição**: ao cancelar um pedido, os itens são devolvidos ao estoque.
- **Máquina de estados do pedido**: transições de status são controladas (`PENDENTE → CONFIRMADO → EM_ROTA → ENTREGUE`, com `CANCELADO` permitido a partir dos três primeiros estados); transições inválidas são rejeitadas.
- **Atribuição de entregador**: só é possível atribuir um entregador `disponível`; ao ser atribuído, ele fica indisponível para novos pedidos até a entrega ser concluída ou cancelada.
- **Exclusão de pedidos**: só é permitida para pedidos ainda `PENDENTE` (os demais devem ser cancelados, preservando o histórico).
- **Unicidade**: e-mail de cliente, CPF de entregador, nome de categoria e nome de usuário não podem se repetir.
- **Integridade referencial de categorias**: uma categoria não pode ser excluída enquanto possuir produtos vinculados.

## Testes automatizados

Testes unitários com **JUnit 5** e **Mockito** cobrindo as principais regras de negócio dos serviços (`ClienteService`, `ProdutoService`, `PedidoService`), incluindo casos de sucesso e de exceção (estoque insuficiente, transições de status inválidas, entregador indisponível, duplicidade de dados, recursos não encontrados).

```bash
./mvnw test
```

Os testes utilizam o perfil `test` (`application-test.properties`), com banco H2 em memória, para não depender de uma instância MySQL durante a execução dos testes.

## Estrutura de pastas

```
src/main/java/com/example/api
├── ApiApplication.java
├── config/            # Segurança, OpenAPI, carga inicial de usuário admin
├── controller/        # Endpoints REST
├── dto/
│   ├── request/        # DTOs de entrada, com validação
│   └── response/       # DTOs de saída
├── exception/         # Exceções de domínio + handler centralizado
├── mapper/            # Conversão entidade <-> DTO
├── model/
│   └── enums/
├── repository/        # Interfaces Spring Data JPA
├── security/          # JWT, filtro de autenticação, UserDetailsService
└── service/
    └── impl/
```

## Roteiro sugerido para apresentação/demonstração

1. Subir a aplicação (`./mvnw spring-boot:run`) e mostrar o Swagger UI.
2. Fazer login com o usuário `admin` e mostrar o token retornado.
3. Cadastrar uma categoria e um produto.
4. Cadastrar um cliente (com endereço) e um entregador.
5. Criar um pedido com dois itens e mostrar a baixa automática de estoque.
6. Tentar criar um pedido com quantidade maior que o estoque disponível, mostrando o erro de regra de negócio.
7. Atribuir o entregador ao pedido e mostrar que ele fica indisponível.
8. Avançar o status do pedido (`CONFIRMADO` → `EM_ROTA` → `ENTREGUE`) e tentar uma transição inválida (ex.: `ENTREGUE` → `EM_ROTA`) para mostrar a validação.
9. Consultar pedidos com filtros (status, cliente, período) e paginação/ordenação.
10. Tentar excluir um cliente com um usuário sem papel `ADMIN`, mostrando o controle de acesso (403).
11. Rodar `./mvnw test` para mostrar a suíte de testes automatizados passando.
