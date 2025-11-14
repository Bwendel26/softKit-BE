# SoftKit Backend

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

API RESTful desenvolvida com Spring Boot para gerenciamento de usuários e autenticação JWT.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Executando o Projeto](#executando-o-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Migrações do Banco de Dados](#migrações-do-banco-de-dados)
- [Docker](#docker)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## 🎯 Sobre o Projeto

SoftKit é uma aplicação backend robusta que fornece:

- Sistema completo de autenticação e autorização com JWT
- Gerenciamento de usuários com diferentes perfis (ADMIN e CUSTOMER)
- Validação de dados com Bean Validation
- Migrações de banco de dados com Flyway
- Segurança com Spring Security
- Mapeamento de objetos com MapStruct

## 🚀 Tecnologias Utilizadas

### Core
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.4** - Framework principal
- **Maven** - Gerenciamento de dependências

### Banco de Dados
- **MySQL 8.0** - Banco de dados relacional
- **Flyway** - Controle de versão do banco de dados
- **Spring Data JPA** - Abstração de acesso a dados
- **Hibernate** - ORM

### Segurança
- **Spring Security** - Framework de segurança
- **JWT (JSON Web Token)** - Autenticação stateless
- **BCrypt** - Criptografia de senhas

### Outras Bibliotecas
- **MapStruct 1.5.5** - Mapeamento de objetos (DTO ↔ Entity)
- **Bean Validation** - Validação de dados
- **Lombok** (recomendado adicionar) - Redução de boilerplate

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

```
┌─────────────────────────────────────┐
│         Controllers                 │  ← Endpoints REST
├─────────────────────────────────────┤
│         Services                    │  ← Lógica de negócio
├─────────────────────────────────────┤
│         Repositories                │  ← Acesso a dados
├─────────────────────────────────────┤
│         Database (MySQL)            │  ← Persistência
└─────────────────────────────────────┘
```

**Padrões Utilizados:**
- DTO (Data Transfer Object)
- VO (Value Object)
- Repository Pattern
- Dependency Injection
- Builder Pattern (via MapStruct)

## 📋 Pré-requisitos

- Java 21 ou superior
- Maven 3.9+
- MySQL 8.0+
- Docker e Docker Compose (opcional)

## 🔧 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/softKit-BE.git
cd softKit-BE
```

### 2. Configure o banco de dados

Crie um banco de dados MySQL:

```sql
CREATE DATABASE softkit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (ou configure no `application.yml`):

```properties
DB_URL=jdbc:mysql://localhost:3306/softkit
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=your-secret-key-here-minimum-256-bits
JWT_EXPIRATION=3600000
```

### 4. Instale as dependências

```bash
./mvnw clean install
```

## ⚙️ Configuração

### application.yml

O arquivo de configuração principal está em `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/softkit}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true

cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:3001
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - PATCH
    - OPTIONS
```

## 🎮 Executando o Projeto

### Localmente

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Com Docker Compose

```bash
docker-compose up -d
```

Isso iniciará:
- Aplicação Spring Boot na porta 8080
- MySQL na porta 3306

Para parar:

```bash
docker-compose down
```

## 📚 Endpoints da API

### Autenticação

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@email.com",
  "password": "password123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

#### Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "João Silva",
  "username": "joao.silva",
  "email": "joao@email.com",
  "phone": "(11) 98765-4321",
  "password": "Senha123!"
}
```

### Usuários

#### Buscar usuário por ID
```http
GET /api/users/{id}
Authorization: Bearer {token}
```

**Requisitos:** Perfil ADMIN

#### Criar usuário
```http
POST /api/users
Authorization: Bearer {token}
Content-Type: application/json

{
  "fullName": "Maria Santos",
  "username": "maria.santos",
  "email": "maria@email.com",
  "phone": "(21) 99876-5432",
  "password": "Senha123!"
}
```

#### Atualizar usuário
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "fullName": "Maria Santos Silva",
  "email": "maria.silva@email.com",
  "phone": "(21) 99999-9999"
}
```

**Requisitos:** Perfil ADMIN ou ser o próprio usuário

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/softKit/softKit_BE/
│   │   ├── config/              # Configurações (Security, CORS, JWT)
│   │   │   ├── CorsProperties.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/          # Controllers REST
│   │   │   └── UserController.java
│   │   ├── exception/           # Exceções customizadas
│   │   │   ├── InvalidJwtAuthenticationException.java
│   │   │   └── handler/
│   │   ├── model/               # Entidades, DTOs, VOs
│   │   │   ├── User.java
│   │   │   ├── Enums/
│   │   │   ├── dto/
│   │   │   │   ├── UserCreateDTO.java
│   │   │   │   ├── UserUpdateDTO.java
│   │   │   │   ├── LoginRequestDTO.java
│   │   │   │   └── LoginResponseDTO.java
│   │   │   ├── vo/
│   │   │   │   └── UserResponseVO.java
│   │   │   └── mapper/
│   │   │       └── UserMapper.java (MapStruct)
│   │   ├── repository/          # Repositórios JPA
│   │   │   └── UserRepository.java
│   │   ├── service/             # Lógica de negócio
│   │   │   ├── UserService.java
│   │   │   └── JwtService.java
│   │   └── SoftKitBeApplication.java
│   └── resources/
│       ├── application.yml      # Configurações da aplicação
│       └── db/migration/        # Migrações Flyway
│           └── V1__create_users_table.sql
└── test/                        # Testes unitários e integração
```

## 🗃️ Migrações do Banco de Dados

O projeto utiliza Flyway para versionamento do banco de dados.

### Estrutura da tabela Users

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    username VARCHAR(150) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    phone VARCHAR(20),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

### Criar nova migração

1. Crie um arquivo em `src/main/resources/db/migration/`
2. Siga o padrão: `V{version}__{description}.sql`
   - Exemplo: `V2__add_user_status_column.sql`

## 🐳 Docker

### Dockerfile

O projeto inclui um Dockerfile multi-stage para build otimizado:

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

### Comandos úteis

```bash
# Build da imagem
docker build -t softkit-be .

# Executar container
docker run -p 8080:8080 softkit-be

# Com Docker Compose
docker-compose up -d
docker-compose logs -f
docker-compose down
```

## 🔒 Segurança

### Senhas

- Todas as senhas são criptografadas com BCrypt
- Requisitos mínimos de senha:
  - Mínimo 8 caracteres
  - Pelo menos 1 letra maiúscula
  - Pelo menos 1 letra minúscula
  - Pelo menos 1 número

### JWT

- Tokens JWT para autenticação stateless
- Expiração configurável (padrão: 1 hora)
- Tokens incluem roles do usuário

### Perfis de Acesso

- **ADMIN**: Acesso completo ao sistema
- **CUSTOMER**: Acesso limitado aos próprios dados

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com cobertura
./mvnw test jacoco:report
```

## 📝 Validações

O projeto utiliza Bean Validation (Jakarta Validation) para validar dados:

### UserCreateDTO
- `fullName`: Obrigatório, máximo 200 caracteres
- `username`: Obrigatório, máximo 150 caracteres
- `email`: Obrigatório, formato email válido, máximo 150 caracteres
- `password`: Obrigatório, mínimo 8 caracteres, deve conter letra maiúscula, minúscula e número
- `phone`: Opcional, máximo 20 caracteres

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Commit

```
feat: adiciona nova funcionalidade
fix: corrige bug
docs: atualiza documentação
style: formatação, sem mudança de código
refactor: refatoração de código
test: adiciona ou corrige testes
chore: atualizações de build, dependências, etc
```

## 🐛 Problemas Conhecidos

- [ ] JWT Secret key deve ser movido para variáveis de ambiente
- [ ] Implementar refresh token
- [ ] Adicionar paginação nos endpoints de listagem
- [ ] Implementar testes de integração
- [ ] Adicionar Swagger/OpenAPI documentation

## 🗺️ Roadmap

- [ ] Implementar sistema de recuperação de senha
- [ ] Adicionar autenticação OAuth2
- [ ] Implementar sistema de logs
- [ ] Adicionar cache com Redis
- [ ] Criar endpoints de relatórios
- [ ] Implementar websockets para notificações em tempo real

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## ✨ Autor

**Bruno Fernandes**

- GitHub: [@seu-usuario](https://github.com/seu-usuario)
- LinkedIn: [seu-linkedin](https://linkedin.com/in/seu-perfil)

## 🙏 Agradecimentos

- Spring Team pela excelente documentação
- Comunidade Java/Spring Boot
- Todos os contribuidores do projeto

---

⭐️ Se este projeto foi útil para você, considere dar uma estrela!

**Feito com ☕ por Bruno Fernandes**
