# Banking System - Core Banking REST API

A production-grade, secure Core Banking System backend built with **Spring Boot 4**, **Java 21**, **PostgreSQL**, **Flyway**, and **Spring Security (JWT)**.

## 🚀 Key Features

- **Authentication & Authorization**:
  - JWT Bearer Token based authentication.
  - Role-based Access Control (`ROLE_ADMIN`, `ROLE_USER` / Teller).
  - BCrypt password hashing.
  - Pre-flight security filters & custom unauthorized JSON error handling.

- **Customer KYC Management**:
  - Full CRUD for bank customer profiles.
  - Dynamic multi-column search (name, email, phone, passport/national ID).
  - Spring Data JPA pagination & dynamic sorting.

- **Account Operations**:
  - Savings and Checking account creation with automated unique account number generation.
  - Transaction ledger: Deposits, Withdrawals, and Inter-account Transfers.
  - Balance safety validations with transaction rollbacks.

- **Audit & Compliance**:
  - Automated audit log capturing user actions, IP addresses, resource targets, and execution timestamps.

- **Database Migration**:
  - Version-controlled schema migrations with **Flyway** (`db/migration/V1__init_schema.sql`).

- **Interactive API Documentation**:
  - Swagger UI & OpenAPI 3.1 documentation.

---

## 🛠️ Tech Stack

- **Language & Framework**: Java 21, Spring Boot 4.1.1
- **Database**: PostgreSQL 18+
- **Database Migration**: Flyway 12.4.0
- **ORM & Data Access**: Hibernate 7, Spring Data JPA
- **Security**: Spring Security 7, JJWT 0.13.0
- **Documentation**: Springdoc OpenAPI / Swagger UI 2.8.5
- **Build Tool**: Apache Maven

---

## 🐳 Docker Quickstart (Recommended)

Run both the Database (PostgreSQL) and the Backend API together with a single command:

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Build and start containers
docker compose up -d --build
```

The services will start automatically:
- **Backend API**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **PostgreSQL Database**: `localhost:5432`

To stop:
```bash
docker compose down
```

---

## 📦 Getting Started (Local Development)

### 1. Prerequisites
- Java 21 (JDK 21)
- Docker (for PostgreSQL) or local PostgreSQL instance
- Maven 3.9+ (or use included `./mvnw`)

### 2. Database Setup
Start PostgreSQL via Docker:
```bash
docker run --name postgre-db \
  -e POSTGRES_DB=banking_system_db \
  -e POSTGRES_USER=banking_system \
  -e POSTGRES_PASSWORD=your_secure_password \
  -p 5432:5432 -d postgres:17-alpine
```

### 3. Local Configuration & Secrets
Sensitive credentials (database password, JWT secret) are never stored in git.
For local development, create `src/main/resources/application-local.yaml` (which is excluded in `.gitignore`):
```yaml
spring:
  datasource:
    password: your_database_password

app:
  jwt:
    secret: your_256_bit_jwt_secret
```

### 4. Run Application
```bash
./mvnw spring-boot:run
```

The application starts on port `8080`.

---

## 📚 API Documentation

Once the application is running, visit:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔐 Core Endpoints Overview

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new user account | Public |
| `POST` | `/api/v1/auth/login` | Authenticate user credentials & return JWT | Public |
| `GET` | `/api/v1/customers` | Get paginated customers with search | Authenticated |
| `POST` | `/api/v1/customers` | Register customer KYC profile | Authenticated |
| `POST` | `/api/v1/accounts` | Open depository account | Authenticated |
| `POST` | `/api/v1/accounts/deposit` | Teller deposit operation | Authenticated |
| `POST` | `/api/v1/accounts/withdraw` | Teller withdrawal operation | Authenticated |
| `POST` | `/api/v1/accounts/transfer` | Inter-account fund transfer | Authenticated |
| `GET` | `/api/v1/audit-logs` | Query system audit logs | Admin Only |

---

## 📄 License
This project is licensed under the Apache License 2.0.
