# SoftKit Backend

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

API RESTful desenvolvida com Spring Boot para **autenticação JWT** e **gestão de usuários**, organizada em módulos de domínio (`auth` e `user`) com uma arquitetura DDD-friendly.

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura](#arquitetura)
- [Domínios e Casos de Uso](#domínios-e-casos-de-uso)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Executando o Projeto](#executando-o-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Migrações do Banco de Dados](#migrações-do-banco-de-dados)
- [Docker](#docker)
- [Segurança](#segurança)
- [Testes](#testes)
- [Contribuindo](#contribuindo)
- [Roadmap](#roadmap)
- [Licença](#licença)

---

## 🎯 Sobre o Projeto

SoftKit é um backend focado em **segurança** e **gestão de usuários**, com:

- Autenticação e autorização **stateless** via JWT
- Gestão de usuários com papéis (`ADMIN`, `CUSTOMER`) e status (`PENDING`, `ACTIVE`, `DISABLED`)
- Bloqueio de conta por tentativas falhas de login
- Infraestrutura para recuperação de senha via **tokens de reset**
- Migrações versionadas com Flyway (PostgreSQL)
- Organização do código em **módulos de domínio** (`auth`, `user`) para facilitar evolução e futura extração para microserviços

---

## 🚀 Tecnologias Utilizadas

### Core

- **Java 21**
- **Spring Boot 3.5.4**
- **Maven** (build e dependências)

### Banco de Dados

- **PostgreSQL 14+**
- **Flyway** (migrações)
- **Spring Data JPA**
- **Hibernate**

### Segurança

- **Spring Security**
- **JWT (JSON Web Token)** – autenticação stateless
- **BCrypt** – hashing de senhas
- Bloqueio de conta por tentativas consecutivas inválidas

### Outras Bibliotecas

- **MapStruct** – mapeamento entre Entities e DTOs
- **Bean Validation (Jakarta Validation)** – validação de campos
- **springdoc-openapi** (ou equivalente) – documentação Swagger/OpenAPI (quando configurado)

---

## 🏗️ Arquitetura

O projeto segue um estilo **DDD-friendly com vertical slices por domínio**, em vez de camadas puramente técnicas:

```text
┌─────────────────────────────────────┐
│           HTTP / Controllers       │  ← auth.api / user.api
├─────────────────────────────────────┤
│      Application / Use Cases       │  ← auth.application / user.application
├─────────────────────────────────────┤
│             Domain                 │  ← Entidades, regras de negócio
├─────────────────────────────────────┤
│        Infrastructure / JPA        │  ← Repositórios, mappers, JWT, DB
└─────────────────────────────────────┘
```

### Módulos principais

- `auth`
    - Autenticação (login, registro, refresh token)
    - Fluxo de recuperação de senha (forgot/reset)
    - Emissão e validação de JWT
- `user`
    - CRUD de usuários
    - Atualização de dados
    - Troca de senha (usuário autenticado)
    - Atualização de status (ADMIN)

- `config`
    - Segurança (Spring Security + JWT filter)
    - Configurações de CORS, web, OpenAPI
    - Handlers de autenticação/autorização

- `exception`
    - Exceções de domínio e técnicas
    - `GlobalExceptionHandler` com resposta padronizada de erro

---

## 🧩 Domínios e Casos de Uso

### Auth

- `POST /api/v1/auth/login`  
  Autenticação com email + senha, retorno de JWT e tempo de expiração.

- `POST /api/v1/auth/register`  
  Registro de novo usuário (perfil padrão `CUSTOMER`).

- `POST /api/v1/auth/forgot-password` *(infra pronta via `PasswordResetToken`)*  
  Geração de token de reset de senha (fluxo de envio de e-mail pode ser plugado).

- `POST /api/v1/auth/reset-password`  
  Reset de senha com base em token válido/não expirado.

- `POST /api/v1/auth/refresh-token`  
  Endpoint previsto para renovação de token (conforme estratégia adotada).

### User

- `GET /api/v1/users/{id}`
- `GET /api/v1/users` (lista, paginável futuramente)
- `POST /api/v1/users` (ADMIN cria usuário)
- `PUT /api/v1/users/{id}` (ADMIN ou o próprio usuário, conforme regra de `@PreAuthorize`)
- `PATCH /api/v1/users/{id}/status` (ADMIN atualiza status)
- `POST /api/v1/users/change-password` (usuário autenticado troca sua senha atual)

---

## 📋 Pré-requisitos

- **Java 21**
- **Maven 3.9+**
- **PostgreSQL 14+**
- Docker / Docker Compose (opcional, para subir stack completa)

---

## 🔧 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/softKit-BE.git
cd softKit-BE
```

### 2. Crie o banco de dados PostgreSQL

```sql
CREATE DATABASE authentication_db;
```

Opcionalmente, definir usuário/senha específicos:

```sql
CREATE USER softkit_user WITH ENCRYPTED PASSWORD 'softkit_pass';
GRANT ALL PRIVILEGES ON DATABASE authentication_db TO softkit_user;
```

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz (ou use variáveis de ambiente do sistema):

```properties
DB_URL=jdbc:postgresql://localhost:5433/authentication_db
DB_USERNAME=softkit_user
DB_PASSWORD=softkit_pass

# JWT secret deve ser uma chave Base64 com pelo menos 32 bytes decodificados
JWT_SECRET=BASE64_ENCODED_SECRET_HERE
JWT_EXPIRATION_MS=3600000
```

> ⚠️ O `JwtService` valida o tamanho mínimo do secret. Use uma chave forte e Base64 válida.

### 4. Instale as dependências

```bash
./mvnw clean install
```

---

## ⚙️ Configuração

Arquivo principal: `src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5433/authentication_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: UTC

  flyway:
    enabled: true
    url: ${DB_URL:jdbc:postgresql://localhost:5433/authentication_db}
    baseline-on-migrate: true
    validate-on-migrate: true
    out-of-order: false
    locations: classpath:db/migration

cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:3001
  allowed-methods:
    - GET
    - POST
    - PUT
    - PATCH
    - DELETE
    - OPTIONS

security:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: ${JWT_EXPIRATION_MS:3600000}
```

---

## 🎮 Executando o Projeto

### Localmente

```bash
./mvnw spring-boot:run
```

A API ficará disponível em: `http://localhost:8080`

### Com Docker Compose

Se houver um `docker-compose.yml` configurando app + PostgreSQL:

```bash
docker-compose up -d
docker-compose logs -f
```

Para parar:

```bash
docker-compose down
```

---

## 📚 Endpoints da API

### Autenticação (`/api/v1/auth`)

#### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@email.com",
  "password": "SenhaFort3!"
}
```

**Resposta:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "fullName": "User Name",
    "email": "user@email.com",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdAt": "2025-01-01T12:00:00"
  }
}
```

#### Registro

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "João Silva",
  "email": "joao@email.com",
  "phoneE164": "+5511999999999",
  "password": "SenhaFort3!"
}
```

**Resposta (201 Created):**

```json
{
  "id": 2,
  "fullName": "João Silva",
  "email": "joao@email.com",
  "phoneE164": "+5511999999999",
  "createdAt": "2025-01-01T12:00:00",
  "updatedAt": "2025-01-01T12:00:00"
}
```

*(Endpoints `forgot-password`, `reset-password` e `refresh-token` podem variar conforme implementação final; consulte a documentação Swagger/OpenAPI gerada pela aplicação.)*

---

### Usuários (`/api/v1/users`)

> Todos os endpoints abaixo requerem `Authorization: Bearer {token}` válido.

#### Buscar usuário por ID (ADMIN)

```http
GET /api/v1/users/{id}
Authorization: Bearer {token}
```

#### Listar usuários (ADMIN)

```http
GET /api/v1/users
Authorization: Bearer {token}
```

*(Paginação pode ser adicionada futuramente via `?page=0&size=20`)*

#### Criar usuário (ADMIN)

```http
POST /api/v1/users
Authorization: Bearer {token}
Content-Type: application/json

{
  "fullName": "Maria Santos",
  "email": "maria@email.com",
  "phoneE164": "+5521999999999",
  "password": "SenhaFort3!"
}
```

#### Atualizar usuário (ADMIN ou o próprio usuário)

```http
PUT /api/v1/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "fullName": "Maria Santos Silva",
  "email": "maria.silva@email.com",
  "phoneE164": "+5521999888877"
}
```

#### Trocar senha (usuário autenticado)

```http
POST /api/v1/users/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "password": "SenhaAntiga1",
  "newPassword": "SenhaNova1",
  "confirmNewPassword": "SenhaNova1"
}
```

---

## 📁 Estrutura do Projeto

```text
src/
├── main/
│   ├── java/com/softKit/softKit_BE/
│   │   ├── SoftKitBeApplication.java
│   │   ├── config/
│   │   │   ├── OpenApiConfig.java          # Configuração Swagger/OpenAPI
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java     # Spring Security + JWT filter chain
│   │   │   │   └── WebConfig.java          # CORS, configurações web
│   │   │   ├── jwt/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtProperties.java
│   │   │   └── exception/
│   │   │       ├── RestAuthenticationEntryPoint.java
│   │   │       └── RestAccessDeniedHandler.java
│   │   ├── auth/
│   │   │   ├── api/
│   │   │   │   ├── AuthController.java
│   │   │   │   └── dto/                    # LoginRequest, LoginResponse, RegisterRequest, etc.
│   │   │   ├── application/
│   │   │   │   ├── AuthService.java
│   │   │   │   └── JwtService.java
│   │   │   ├── domain/
│   │   │   │   ├── PasswordResetToken.java
│   │   │   │   └── mapper/
│   │   │   │       └── AuthMapper.java
│   │   │   └── infrastructure/
│   │   │       └── PasswordResetTokenRepository.java
│   │   ├── user/
│   │   │   ├── api/
│   │   │   │   ├── UserController.java
│   │   │   │   └── dto/                    # UserCreateRequest, UserUpdateRequest, UserResponse, etc.
│   │   │   ├── application/
│   │   │   │   └── UserService.java
│   │   │   ├── domain/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Status.java
│   │   │   │   └── mapper/
│   │   │   │       └── UserMapper.java
│   │   ├── exception/
│   │   │   ├── dto/
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── EmailAlreadyInUseException.java
│   │   │   ├── UserNotFoundException.java
│   │   │   ├── InvalidJwtAuthenticationException.java
│   │   │   └── InvalidTokenException.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           └── V1__init_users_and_password_reset_tokens.sql
└── test/
    └── java/com/softKit/softKit_BE/
        ├── auth/   # Testes de AuthController/AuthService/JwtService
        ├── user/   # Testes de UserController/UserService
        └── config/ # TestSecurityConfig, etc.
```

---

## 🗃️ Migrações do Banco de Dados

O projeto utiliza **Flyway** para controlar a evolução do schema.

### Migração inicial (`V1__init_users_and_password_reset_tokens.sql`)

Exemplo simplificado do schema (PostgreSQL):

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    email_verified_at TIMESTAMP(3),
    password_hash VARCHAR(255) NOT NULL,
    phone_e164 VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failed_login_attempts INTEGER NOT NULL DEFAULT 0 CHECK (failed_login_attempts >= 0),
    locked_until TIMESTAMP(3),
    last_login_at TIMESTAMP(3),
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('PENDING', 'ACTIVE', 'DISABLED'))
);

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    used_at TIMESTAMP(3),
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```
### DUMP de dados pra testes locais
```sql
INSERT INTO users (full_name, username, email, password, profile, phone, created_at, updated_at) VALUES
('Ana Silva Santos', 'ana.silva', 'ana.silva@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(11) 98765-4321', NOW(), NOW()),
('Bruno Costa Oliveira', 'bruno.costa', 'bruno.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(21) 99876-5432', NOW(), NOW()),
('Carla Fernandes Lima', 'carla.fernandes', 'carla.fernandes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(31) 97654-3210', NOW(), NOW()),
('Daniel Rodrigues Souza', 'daniel.rodrigues', 'daniel.rodrigues@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(41) 96543-2109', NOW(), NOW()),
('Eduarda Martins Alves', 'eduarda.martins', 'eduarda.martins@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(51) 95432-1098', NOW(), NOW()),
('Felipe Gomes Pereira', 'felipe.gomes', 'felipe.gomes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(61) 94321-0987', NOW(), NOW()),
('Gabriela Santos Costa', 'gabriela.santos', 'gabriela.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(71) 93210-9876', NOW(), NOW()),
('Henrique Lima Oliveira', 'henrique.lima', 'henrique.lima@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(81) 92109-8765', NOW(), NOW()),
('Isabela Alves Fernandes', 'isabela.alves', 'isabela.alves@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(85) 91098-7654', NOW(), NOW()),
('João Pereira Silva', 'joao.pereira', 'joao.pereira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(11) 90987-6543', NOW(), NOW()),
('Karen Costa Rodrigues', 'karen.costa', 'karen.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(21) 89876-5432', NOW(), NOW()),
('Lucas Souza Martins', 'lucas.souza', 'lucas.souza@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(31) 88765-4321', NOW(), NOW()),
('Mariana Oliveira Gomes', 'mariana.oliveira', 'mariana.oliveira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(41) 87654-3210', NOW(), NOW()),
('Nicolas Santos Lima', 'nicolas.santos', 'nicolas.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(51) 86543-2109', NOW(), NOW()),
('Olivia Fernandes Costa', 'olivia.fernandes', 'olivia.fernandes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(61) 85432-1098', NOW(), NOW()),
('Pedro Alves Rodrigues', 'pedro.alves', 'pedro.alves@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(71) 84321-0987', NOW(), NOW()),
('Quésia Lima Santos', 'quesia.lima', 'quesia.lima@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(81) 83210-9876', NOW(), NOW()),
('Rafael Costa Pereira', 'rafael.costa', 'rafael.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(85) 82109-8765', NOW(), NOW()),
('Sofia Martins Oliveira', 'sofia.martins', 'sofia.martins@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(11) 81098-7654', NOW(), NOW()),
('Thiago Gomes Silva', 'thiago.gomes', 'thiago.gomes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(21) 80987-6543', NOW(), NOW()),
('Ursula Santos Fernandes', 'ursula.santos', 'ursula.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(31) 79876-5432', NOW(), NOW()),
('Vitor Lima Costa', 'vitor.lima', 'vitor.lima@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(41) 78765-4321', NOW(), NOW()),
('Wanda Alves Rodrigues', 'wanda.alves', 'wanda.alves@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(51) 77654-3210', NOW(), NOW()),
('Xavier Pereira Martins', 'xavier.pereira', 'xavier.pereira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(61) 76543-2109', NOW(), NOW()),
('Yasmin Souza Gomes', 'yasmin.souza', 'yasmin.souza@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(71) 75432-1098', NOW(), NOW()),
('Zeca Oliveira Santos', 'zeca.oliveira', 'zeca.oliveira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(81) 74321-0987', NOW(), NOW()),
('Amanda Silva Costa', 'amanda.silva', 'amanda.silva@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(85) 73210-9876', NOW(), NOW()),
('Bernardo Costa Lima', 'bernardo.costa', 'bernardo.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(11) 72109-8765', NOW(), NOW()),
('Camila Fernandes Alves', 'camila.fernandes', 'camila.fernandes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(21) 71098-7654', NOW(), NOW()),
('Diego Rodrigues Pereira', 'diego.rodrigues', 'diego.rodrigues@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(31) 70987-6543', NOW(), NOW()),
('Elisa Martins Silva', 'elisa.martins', 'elisa.martins@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(41) 69876-5432', NOW(), NOW()),
('Fabio Gomes Souza', 'fabio.gomes', 'fabio.gomes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(51) 68765-4321', NOW(), NOW()),
('Giovana Santos Oliveira', 'giovana.santos', 'giovana.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(61) 67654-3210', NOW(), NOW()),
('Hugo Lima Fernandes', 'hugo.lima', 'hugo.lima@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(71) 66543-2109', NOW(), NOW()),
('Ingrid Alves Costa', 'ingrid.alves', 'ingrid.alves@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(81) 65432-1098', NOW(), NOW()),
('Julio Pereira Rodrigues', 'julio.pereira', 'julio.pereira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(85) 64321-0987', NOW(), NOW()),
('Karina Costa Martins', 'karina.costa', 'karina.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(11) 63210-9876', NOW(), NOW()),
('Leonardo Souza Gomes', 'leonardo.souza', 'leonardo.souza@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(21) 62109-8765', NOW(), NOW()),
('Monica Oliveira Santos', 'monica.oliveira', 'monica.oliveira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(31) 61098-7654', NOW(), NOW()),
('Natanael Santos Lima', 'natanael.santos', 'natanael.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(41) 60987-6543', NOW(), NOW()),
('Otávio Fernandes Alves', 'otavio.fernandes', 'otavio.fernandes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(51) 59876-5432', NOW(), NOW()),
('Patrícia Lima Pereira', 'patricia.lima', 'patricia.lima@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(61) 58765-4321', NOW(), NOW()),
('Renan Alves Silva', 'renan.alves', 'renan.alves@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(71) 57654-3210', NOW(), NOW()),
('Sabrina Costa Rodrigues', 'sabrina.costa', 'sabrina.costa@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(81) 56543-2109', NOW(), NOW()),
('Tiago Martins Souza', 'tiago.martins', 'tiago.martins@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(85) 55432-1098', NOW(), NOW()),
('Valentina Gomes Oliveira', 'valentina.gomes', 'valentina.gomes@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(11) 54321-0987', NOW(), NOW()),
('Wagner Santos Costa', 'wagner.santos', 'wagner.santos@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'ADMIN', '(21) 53210-9876', NOW(), NOW()),
('Ximena Lima Fernandes', 'ximena.lima', 'ximena.lima@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(31) 52109-8765', NOW(), NOW()),
('Yuri Alves Santos', 'yuri.alves', 'yuri.alves@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(41) 51098-7654', NOW(), NOW()),
('Zilda Pereira Lima', 'zilda.pereira', 'zilda.pereira@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa', 'CUSTOMER', '(51) 50987-6543', NOW(), NOW());
```

## 🔑 Informações de Acesso para Teste

Perfil,Exemplo de Email,Senha (Texto Simples)
ADMIN,carla.fernandes@email.com,123456
CUSTOMER,ana.silva@email.com,123456
## ❗Note que todos os usuários, independentemente do perfil, usam a mesma senha de hash. A senha em texto simples correspondente ao hash $2a$10$N9qo8uLOickgx2ZMRZoMye1J8Rm/C2AVqVOCSmL7mqyXyJRv0qXUa é 123456.

Para criar novas migrações:

1. Criar um arquivo em `src/main/resources/db/migration/`
2. Seguir o padrão: `V{versão}__{descrição}.sql`  
   Ex.: `V2__add_audit_logs_table.sql`

---

## 🐳 Docker

### Dockerfile (multi-stage)

```dockerfile
# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /softKit-BE
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /softKit-BE
COPY --from=build /softKit-BE/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Comandos úteis:

```bash
# Build da imagem
docker build -t softkit-be .

# Rodar container
docker run -p 8080:8080 --env-file .env softkit-be
```

---

## 🔒 Segurança

### Senhas

- Hash com **BCrypt**
- Regras de senha (padrão):
    - Mínimo 8 caracteres
    - Pelo menos 1 letra maiúscula
    - Pelo menos 1 letra minúscula
    - Pelo menos 1 dígito numérico

### JWT

- Tokens assinados com chave HMAC (HS256)
- Chave secreta **Base64** com tamanho mínimo validado pelo `JwtService`
- Tempo de expiração configurável (`security.jwt.expiration-ms`)
- Inclusão de roles nas claims (`roles`)

### Perfis e Status

- **Roles**:
    - `ADMIN`: acesso administrativo
    - `CUSTOMER`: usuário final
- **Status**:
    - `PENDING` → conta criada, ainda não ativada
    - `ACTIVE` → conta ativa
    - `DISABLED` → acesso bloqueado

---

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com relatório de cobertura (JaCoCo)
./mvnw test jacoco:report
```

O objetivo é manter **cobertura mínima de 80%**, com foco em:

- `AuthService`, `UserService`, `JwtService`
- `AuthController`, `UserController`
- `GlobalExceptionHandler`

---

## 🤝 Contribuindo

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`)
3. Commit suas alterações (`git commit -m 'feat: adiciona minha-feature'`)
4. Envie para o repositório remoto (`git push origin feature/minha-feature`)
5. Abra um Pull Request

### Padrão de commits (sugestão)

```text
feat: adiciona nova funcionalidade
fix: corrige bug
docs: atualiza documentação
refactor: refatoração interna de código
test: adiciona ou ajusta testes
chore: tarefas de build, CI, dependências, etc.
```

---

## 🗺️ Roadmap

- [ ] Implementar fluxo completo de refresh token
- [ ] Completar fluxo de recuperação de senha (integração com e-mail)
- [ ] Paginação e filtros em `/api/v1/users`
- [ ] Testes de integração com banco em memória / Testcontainers
- [ ] Monitoramento e observabilidade (logs estruturados, métricas)
- [ ] Cache para endpoints mais acessados (ex.: Redis)

---

## ✨ Autores

**Bruno Fernandes**

- GitHub: [@Bwendel](https://github.com/Bwendel26/)
- LinkedIn: [Bruno Fernandes](https://linkedin.com/in/seu-perfil)

**Francisco Camello**

- GitHub: [@FranciscoCamellon](https://github.com/franciscocamellon)
- LinkedIn: [Francisco Camello](https://www.linkedin.com/in/franciscocamellon/)

---

## 📄 Licença

Este projeto está sob a licença **MIT**. Consulte o arquivo `LICENSE` para mais detalhes.

Se este projeto te ajudou, considere deixar uma ⭐ no repositório 🙂
