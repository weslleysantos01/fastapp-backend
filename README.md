# 🎉 FestApp — Backend

API REST do **FestApp**, um sistema SaaS para gerenciamento de locações de itens para festas e eventos. Desenvolvido com Spring Boot e MySQL.

---

## 🛠️ Tecnologias

- [Java 17+](https://www.oracle.com/java/)
- [Spring Boot 3](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security) (JWT + BCrypt)
- [Spring Data JPA / Hibernate](https://spring.io/projects/spring-data-jpa)
- [MySQL](https://www.mysql.com/)
- [Maven](https://maven.apache.org/)

---

## 📁 Estrutura do Projeto

```
festapp-backend/
├── src/
│   └── main/
│       ├── java/com/festapp/
│       │   ├── config/         # Configurações de segurança, CORS, JWT
│       │   ├── controller/     # Endpoints REST
│       │   ├── dto/            # Data Transfer Objects
│       │   ├── entity/         # Entidades JPA
│       │   ├── repository/     # Interfaces JPA Repository
│       │   ├── service/        # Regras de negócio
│       │   └── FestAppApplication.java
│       └── resources/
│           ├── application.properties
│           └── application-prod.properties
├── pom.xml
└── README.md
```

---

## ⚙️ Configuração e Instalação

### Pré-requisitos

- Java 17+
- Maven 3.8+
- MySQL 8+

### 1. Clone o repositório

```bash
git clone https://github.com/weslleysantos01/festapp-backend.git
cd festapp-backend
```

### 2. Configure o banco de dados

Crie o banco de dados no MySQL:

```sql
CREATE DATABASE festapp;
```

### 3. Configure o `application.properties`

```properties
# Banco de dados
spring.datasource.url=jdbc:mysql://localhost:3306/festapp?useSSL=false&serverTimezone=America/Sao_Paulo
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT
jwt.secret=SEU_SECRET_AQUI
jwt.expiration=86400000

# Servidor
server.port=8080
```

> ⚠️ Nunca commite senhas ou secrets reais. Use variáveis de ambiente em produção.

### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

A API estará disponível em: [http://localhost:8080](http://localhost:8080)

---

## 🔐 Segurança

- **Autenticação:** JWT (JSON Web Token)
- **Senhas:** Hashing com BCrypt
- **Rate Limiting:** Proteção contra força bruta
- **Validação de entrada:** Bean Validation (`@Valid`) em todos os endpoints
- **Proteção contra SQL Injection:** Queries via JPA/JPQL parametrizadas
- **CORS:** Configurado para aceitar apenas origens autorizadas

---

## 📡 Principais Endpoints

### Autenticação

| Método | Endpoint         | Descrição              |
|--------|-----------------|------------------------|
| POST   | `/auth/login`   | Login e geração do JWT |
| POST   | `/auth/register`| Cadastro de usuário    |

### Clientes

| Método | Endpoint            | Descrição               |
|--------|---------------------|-------------------------|
| GET    | `/clientes`         | Listar todos            |
| GET    | `/clientes/{id}`    | Buscar por ID           |
| POST   | `/clientes`         | Criar cliente           |
| PUT    | `/clientes/{id}`    | Atualizar cliente       |
| DELETE | `/clientes/{id}`    | Remover cliente         |

### Locações

| Método | Endpoint            | Descrição               |
|--------|---------------------|-------------------------|
| GET    | `/locacoes`         | Listar todas            |
| GET    | `/locacoes/{id}`    | Buscar por ID           |
| POST   | `/locacoes`         | Criar locação           |
| PUT    | `/locacoes/{id}`    | Atualizar locação       |
| DELETE | `/locacoes/{id}`    | Remover locação         |

> Todos os endpoints (exceto `/auth/**`) exigem o header:
> ```
> Authorization: Bearer <token>
> ```

---

## 🗄️ Banco de Dados

O Spring Boot com `ddl-auto=update` cria e atualiza as tabelas automaticamente. Para produção, recomenda-se usar `ddl-auto=validate` e gerenciar as migrations manualmente com [Flyway](https://flywaydb.org/) ou [Liquibase](https://www.liquibase.org/).

---

## 📦 Build para Produção

```bash
mvn clean package -DskipTests
java -jar target/festapp-0.0.1-SNAPSHOT.jar
```

---

## 🌐 Integração com o Frontend

Este backend é consumido pelo [FestApp Frontend](https://github.com/weslleysantos01/festapp-frontend).

Certifique-se de configurar o CORS corretamente no `SecurityConfig` para o endereço do frontend:

```java
.allowedOrigins("http://localhost:5173")
```

---

---

## 📄 Licença

Este projeto é **proprietário e confidencial**. Todos os direitos reservados © Weslley Santos.

É **estritamente proibido** copiar, modificar, distribuir, sublicenciar ou utilizar este código, no todo ou em parte, sem autorização prévia e expressa do autor.

---

> Desenvolvido por [Weslley Santos](https://github.com/weslleysantos01)
