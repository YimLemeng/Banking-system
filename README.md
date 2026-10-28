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

## 🏛️ System Architecture & Workflow Diagrams

### 1. High-Level Architecture Overview

```mermaid
flowchart TD
    Client["Client (Web / Mobile / Teller UI)"]

    subgraph Security["Spring Security & Filter Pipeline"]
        CorsFilter["CorsFilter"]
        JwtFilter["JwtAuthenticationFilter"]
        AuthManager["AuthenticationManager & DaoAuthenticationProvider"]
    end

    subgraph API["REST Controller Layer"]
        AuthCtrl["AuthController (/api/v1/auth)"]
        CustCtrl["CustomerController (/api/v1/customers)"]
        AccCtrl["AccountController (/api/v1/accounts)"]
        TxCtrl["TransactionController (/api/v1/transactions)"]
        AuditCtrl["AuditLogController (/api/v1/audit-logs)"]
    end

    subgraph ServiceLayer["Service Layer (Business Logic & Transactions)"]
        AuthService["AuthServiceImpl"]
        CustService["CustomerServiceImpl"]
        AccService["AccountServiceImpl"]
        TxService["TransactionServiceImpl"]
        AuditAspect["@Audited Aspect (AOP Logging)"]
    end

    subgraph Persistence["Data Persistence Layer"]
        Repo["Spring Data JPA Repositories"]
        Flyway["Flyway Migration (V1__init_schema.sql)"]
    end

    DB[("PostgreSQL Database (Docker)")]

    Client -->|HTTPS / REST Requests| CorsFilter
    CorsFilter --> JwtFilter
    JwtFilter -->|Extract & Validate Token| AuthManager
    AuthManager --> API
    API --> ServiceLayer
    ServiceLayer -.->|Audit Event Interception| AuditAspect
    AuditAspect --> Repo
    ServiceLayer --> Repo
    Flyway -->|Schema Verification| DB
    Repo <-->|Hibernate 7 / HikariCP| DB
```

---

### 2. Authentication & JWT Token Issuance Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as Client / Teller
    participant AuthCtrl as AuthController (/api/v1/auth/login)
    participant AuthMgr as AuthenticationManager
    participant DaoProv as DaoAuthenticationProvider
    participant UserDetails as CustomUserDetailsService
    participant DB as PostgreSQL Database
    participant JwtService as JwtService

    User->>AuthCtrl: POST /api/v1/auth/login { identifier, password }
    AuthCtrl->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>DaoProv: authenticate(authentication)
    DaoProv->>UserDetails: loadUserByUsername(identifier)
    UserDetails->>DB: Query user by username, email or phone
    DB-->>UserDetails: User Entity (Hashed Password, Role)
    UserDetails-->>DaoProv: UserPrincipal (UserDetails)
    DaoProv->>DaoProv: BCrypt.checkpw(rawPassword, hashedPassword)

    alt Invalid Credentials
        DaoProv-->>User: 401 Unauthorized (Invalid username or password)
    else Credentials Match
        DaoProv-->>AuthMgr: Authentication (Authenticated = true)
        AuthMgr-->>AuthCtrl: Authenticated Principal
        AuthCtrl->>JwtService: generateToken(userDetails)
        Note over JwtService: HMAC-SHA384 Signature<br/>Claims: Subject, Role, Expiration
        JwtService-->>AuthCtrl: Signed JWT Bearer Token
        AuthCtrl-->>User: 200 OK { token, tokenType: "Bearer", username, role }
    end
```

---

### 3. Core Banking Fund Transfer & Ledger Transaction Flow

```mermaid
sequenceDiagram
    autonumber
    actor Teller as Teller / Bank Officer
    participant TxCtrl as AccountController (/api/v1/accounts/transfer)
    participant TxService as AccountServiceImpl / TransactionService
    participant AccRepo as AccountRepository
    participant TxRepo as TransactionRepository
    participant AuditLog as AuditLogService
    participant DB as PostgreSQL Database

    Teller->>TxCtrl: POST /api/v1/accounts/transfer { sourceAccount, targetAccount, amount }
    TxCtrl->>TxService: transfer(request)
    Note over TxService: @Transactional (Atomic Boundary)

    TxService->>AccRepo: findByAccountNumber(sourceAccount)
    AccRepo->>DB: SELECT * FROM accounts WHERE account_number = ?
    DB-->>TxService: Source Account Entity

    TxService->>AccRepo: findByAccountNumber(targetAccount)
    AccRepo->>DB: SELECT * FROM accounts WHERE account_number = ?
    DB-->>TxService: Target Account Entity

    alt Insufficient Balance / Frozen Account
        TxService-->>Teller: 400 Bad Request / InsufficientBalanceException
    else Balance & Account Status Valid
        TxService->>TxService: sourceBalance -= amount
        TxService->>TxService: targetBalance += amount
        TxService->>AccRepo: save(sourceAccount) & save(targetAccount)
        TxService->>TxRepo: save(bankTransactionRecord)
        TxService->>AuditLog: logEvent("TRANSFER", "Account", sourceAccount, "Transferred $amount")
        DB-->>TxService: Commit Transaction (Atomicity Guaranteed)
        TxService-->>TxCtrl: TransactionResponse
        TxCtrl-->>Teller: 200 OK { transactionId, balanceAfter, timestamp }
    end
```

---

### 4. Entity-Relationship Diagram (Database Schema)

```mermaid
erDiagram
    USERS ||--o{ CUSTOMERS : "registers"
    USERS ||--o{ AUDIT_LOGS : "triggers"
    CUSTOMERS ||--o{ ACCOUNTS : "owns"
    ACCOUNTS ||--o{ BANK_TRANSACTIONS : "source account"
    ACCOUNTS ||--o{ BANK_TRANSACTIONS : "target account"

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar phone_number UK
        varchar password
        varchar role
        timestamp created_at
        timestamp updated_at
    }

    CUSTOMERS {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email UK
        varchar phone_number
        varchar national_or_passport
        varchar address
        bigint user_id FK
        timestamp created_at
        timestamp updated_at
    }

    ACCOUNTS {
        bigint id PK
        varchar account_number UK
        varchar account_type
        decimal balance
        varchar status
        bigint customer_id FK
        timestamp created_at
        timestamp updated_at
    }

    BANK_TRANSACTIONS {
        bigint id PK
        varchar transaction_id UK
        varchar type
        decimal amount
        decimal balance_after
        bigint account_id FK
        bigint target_account_id FK
        varchar description
        timestamp timestamp
    }

    AUDIT_LOGS {
        bigint id PK
        varchar username
        varchar action
        varchar resource
        varchar resource_id
        varchar ip_address
        varchar status
        bigint execution_time_ms
        timestamp created_at
    }
```

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
