# Banking System - Core Banking REST API

A production-grade, secure Core Banking System backend built with **Spring Boot 4**, **Java 21**, **PostgreSQL**, **Flyway**, and **Spring Security (JWT)**.

> 🌐 **Live Web Client (UI)**: [https://banking-system.yimlemeng.workers.dev](https://banking-system.yimlemeng.workers.dev)  
> 📚 **API Swagger Documentation**: [https://lemeng-banking-api.duckdns.org/swagger-ui.html](https://lemeng-banking-api.duckdns.org/swagger-ui.html)  
> 🏛️ **Frontend UI Repository**: [https://github.com/YimLemeng/Banking-system-UI](https://github.com/YimLemeng/Banking-system-UI)  
> 👤 **Test Accounts**: `admin` / `admin123` (Admin) • `teller` / `teller123` (Teller)

---

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

## 🔐 Security Architecture & Request Flow

The backend uses **stateless token-based authentication** powered by Spring Security 7 and JSON Web Tokens (JJWT). The server does not store user sessions in memory; instead, every client request carries a digitally signed cryptographic token.

### 1. High-Level Request Pipeline

```mermaid
flowchart TD
    Client["Client (React Web / Teller Terminal)"]
    
    subgraph SpringSecurity ["Spring Security 7 (Stateless Layer)"]
        Cors["1. CorsFilter (CORS Header Check)"]
        JwtFilter["2. JwtAuthenticationFilter (Extract & Verify)"]
        JwtProvider["jwtService (HMAC-SHA384 & Claims)"]
        SecCtx["SecurityContextHolder (Principal & Authorities)"]
        AuthZ["3. AuthorizationFilter (Role Verification)"]
    end
    
    subgraph SpringMVC ["Spring Boot Application Layers"]
        Dispatcher["DispatcherServlet"]
        Controller["REST Controller (@RestController)"]
        Service["Service Layer (@Service - Business Logic)"]
        Repo["Data Access Layer (@Repository - JPA / Hibernate)"]
    end
    
    subgraph Database ["Persistence Layer"]
        Postgres[("PostgreSQL Database")]
    end
    
    Client -->|"HTTP Request + Header: Authorization: Bearer <token>"| Cors
    Cors --> JwtFilter
    JwtFilter <-->|"Validate Signature & Claims"| JwtProvider
    JwtFilter -->|"Set Authentication"| SecCtx
    JwtFilter --> AuthZ
    AuthZ -->|"Authorized"| Dispatcher
    AuthZ -.->|"Unauthorized (401) / Forbidden (403)"| Client
    Dispatcher --> Controller
    Controller --> Service
    Service --> Repo
    Repo <--> Postgres
    Controller -->|"Structured JSON Response"| Client
```

---

### 2. User Authentication Flow (Login & Token Issuance)

When a user signs in, credentials are submitted, verified with BCrypt against the PostgreSQL database, and a cryptographically signed JWT is returned to the client:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (React Web / Teller Terminal)
    participant AuthCtrl as AuthController (/api/v1/auth/login)
    participant AuthSvc as AuthServiceImpl
    participant AuthMgr as AuthenticationManager
    participant DaoProv as DaoAuthenticationProvider
    participant UserDetails as CustomUserDetailsService
    participant DB as PostgreSQL Database
    participant JwtProv as jwtService

    Client->>AuthCtrl: POST /api/v1/auth/login { identifier, password }
    AuthCtrl->>AuthSvc: login(loginRequest)
    AuthSvc->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>DaoProv: authenticate(authentication)
    DaoProv->>UserDetails: loadUserByUsername(identifier)
    UserDetails->>DB: SELECT user WHERE username=? OR email=? OR phone_number=?
    DB-->>UserDetails: User Entity (id, username, email, passwordHash, role)
    UserDetails-->>DaoProv: UserPrincipal (UserDetails with ROLE_ADMIN / ROLE_USER)
    DaoProv->>DaoProv: BCrypt.checkpw(rawPassword, passwordHash)
    
    alt Password Mismatch / User Not Found
        DaoProv-->>Client: 401 Unauthorized / BadCredentialsException
    else Credentials Match
        DaoProv-->>AuthMgr: Authentication (Authenticated = true)
        AuthMgr-->>AuthSvc: Authentication object
        AuthSvc->>JwtProv: generateToken(userDetails)
        Note right of JwtProv: Assembles JWT Token:<br/>- Header: { alg: HS384, typ: JWT }<br/>- Payload: { sub: identifier, role: [...], exp: timestamp }<br/>- Signature: HMAC-SHA384(header.payload, secretKey)
        JwtProv-->>AuthSvc: Signed JWT String
        AuthSvc-->>AuthCtrl: AuthResponse(token, tokenType="Bearer")
        AuthCtrl-->>Client: 200 OK + { token: "eyJhbGci...", tokenType: "Bearer", username, role }
        Note over Client: Stores Bearer token in localStorage for subsequent API requests
    end
```

---

### 3. Authenticated Request Flow (Accessing Protected Endpoints)

For subsequent requests (e.g. depositing cash, withdrawing funds, creating accounts, transferring balances), the client sends the token in the `Authorization` header:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (React Web / Teller Terminal)
    participant FilterChain as Spring Security Filter Chain
    participant JwtFilter as JwtAuthenticationFilter
    participant JwtProv as jwtService
    participant UserDetails as CustomUserDetailsService
    participant SecContext as SecurityContextHolder
    participant Dispatcher as DispatcherServlet
    participant Controller as REST Controller (e.g. AccountController)
    participant Service as Service Layer (e.g. AccountServiceImpl)
    participant Repo as JPA Repository (AccountRepository)
    participant DB as PostgreSQL Database

    Client->>FilterChain: HTTP Request (e.g. POST /api/v1/accounts/transfer)<br/>Header: Authorization: Bearer <jwt_token>
    FilterChain->>JwtFilter: doFilterInternal(request, response, filterChain)
    JwtFilter->>JwtFilter: extractToken() -> "eyJhbGci..."
    
    alt Token Missing or Expired/Invalid
        JwtFilter-->>Client: 401 Unauthorized (via AuthenticationEntryPoint)
    else Token Signature & Expiry Valid
        JwtFilter->>JwtProv: validateToken(jwt)
        JwtProv-->>JwtFilter: true
        JwtFilter->>JwtProv: extractUsername(jwt)
        JwtProv-->>JwtFilter: username
        JwtFilter->>UserDetails: loadUserByUsername(username)
        UserDetails-->>JwtFilter: UserPrincipal (Roles: [ROLE_ADMIN, ...])
        JwtFilter->>SecContext: setAuthentication(UsernamePasswordAuthenticationToken)
        JwtFilter->>FilterChain: filterChain.doFilter(request, response)
        
        alt Insufficient Role Permissions (e.g. ROLE_USER accessing ADMIN audit logs)
            FilterChain-->>Client: 403 Forbidden (via AccessDeniedHandler)
        else Role Authorized
            FilterChain->>Dispatcher: Forward to DispatcherServlet
            Dispatcher->>Controller: Route to target @PostMapping / @GetMapping method
            Controller->>Service: Execute core banking business logic (transfer, deposit, withdraw)
            Service->>Repo: Data access operations (save / find)
            Repo->>DB: Execute SQL Queries (INSERT, UPDATE, SELECT)
            DB-->>Repo: SQL Results
            Repo-->>Service: Managed Entity
            Service-->>Controller: Response DTO
            Controller-->>Client: 200 OK / 201 Created (JSON Response)
        end
    end
```

---

### 4. End-to-End Workflow Summary

1. **User Authentication (Login)**:
   - **Client Request**: The client application (React Web or Teller Terminal) sends `identifier` (username, email, or phone) and `password` via `POST /api/v1/auth/login`.
   - **Credential Verification**: `AuthenticationManager` delegates to `DaoAuthenticationProvider`, which fetches user credentials through `CustomUserDetailsService` and validates the password against the stored database hash using `BCrypt`.
   - **Token Generation**: Upon successful authentication, `jwtService` crafts a cryptographically signed **JWT Access Token** embedded with the subject, role (`ROLE_ADMIN`, `ROLE_USER`), issue timestamp, and expiration time, digitally signed using HMAC-SHA384.
   - **Token Storage**: The server returns an `AuthResponse` object containing the `token`. The client securely stores the token in `localStorage` to authenticate subsequent requests.

2. **Authenticated Client Request Execution**:
   - **Header Attachment**: For every subsequent protected API call (e.g., managing customer KYC, opening accounts, executing deposits/withdrawals/transfers), the client attaches the HTTP header `Authorization: Bearer <token>`.
   - **Filter Interception**: `JwtAuthenticationFilter` intercepts the request before it reaches any controller:
     - **Missing or Expired Token**: Immediately rejected with **401 Unauthorized** via custom `AuthenticationEntryPoint`.
     - **Valid Token**: Extracts user details, constructs a `UsernamePasswordAuthenticationToken`, and injects it into `SecurityContextHolder`.
   - **Role-Based Authorization (RBAC)**: Spring Security verifies endpoint permissions:
     - **Insufficient Privileges** (e.g., a `ROLE_USER` attempting to delete a customer profile or inspect system audit logs): Immediately rejected with **403 Forbidden**.
     - **Authorized**: The request is routed through the Spring MVC pipeline:
       `DispatcherServlet` &rarr; `@RestController` &rarr; `@Service` (Business Logic & Atomic `@Transactional`) &rarr; `@Repository` (JPA / Hibernate) &rarr; `PostgreSQL Database`.
   - **Structured Response**: The business logic executes, ledger updates are committed, and the controller returns structured JSON with HTTP status code `200 OK` or `201 Created` back to the client.

---

### 5. Entity-Relationship Diagram (Database Schema)

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

Interactive OpenAPI / Swagger UI documentation is available at:
- **Production Server**: [https://lemeng-banking-api.duckdns.org/swagger-ui.html](https://lemeng-banking-api.duckdns.org/swagger-ui.html)
- **Localhost Development**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

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
