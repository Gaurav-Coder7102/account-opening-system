# 🏦 Banking Account Opening System (AOS) - Project Context

> **Project Name**: Banking Account Opening System  
> **Architecture**: Microservices (Multi-module Maven Reactor)  
> **Tech Stack**: Java 21, Spring Boot 3.3.x, Spring Cloud OpenFeign, Spring Security 6, PostgreSQL 16, Flyway 10, OpenAPI 3  
> **Database Strategy**: Database-per-Service (Isolated Schemas / Databases)  

---

## 📋 1. Executive Summary

The **Banking Account Opening System (AOS)** is an enterprise-grade, microservices-based banking platform designed for modern online customer onboarding and savings account opening workflows. It features secure user authentication (JWT-based), customer profile & KYC management with strict validation, state-machine orchestration for account opening applications, and automated, thread-safe account number generation.

---

## 🏛️ 2. Architecture & Design Principles

```
                               +-------------------------+
                               |      API Gateway        |
                               |  (Orchestrator / Client)|
                               +------------+------------+
                                            |
                 +--------------------------+--------------------------+--------------------------+
                 |                          |                          |                          |
                 v                          v                          v                          v
     +-----------------------+  +-----------------------+  +-----------------------+  +-----------------------+
     | authentication-service|  |    customer-service   |  |account-opening-service|  |savings-account-service|
     |      (Port 8081)      |  |      (Port 8082)      |  |      (Port 8083)      |  |      (Port 8084)      |
     +-----------+-----------+  +-----------+-----------+  +-----------+-----------+  +-----------+-----------+
                 |                          |                          |                          |
                 v                          v                          v (OpenFeign)              v
         [ auth_db Postgres ]      [ customer_db Postgres ]            +---------------> [ savings_db Postgres ]
                                                                       v
                                                           [ account_db Postgres ]
```

### Key Architectural Principles:
1. **Database-per-Service**: Each microservice manages its own isolated PostgreSQL database/schema to enforce domain decoupling.
2. **Stateless JWT Security**: Passports of identity are issued as signed JWT tokens from `authentication-service` and verified statelessly across services via `common-lib`.
3. **Synchronous Inter-Service Communication**: `account-opening-service` uses **Spring Cloud OpenFeign** to synchronously verify Customer existence and KYC validation (`VERIFIED`) before approving applications.
4. **State-Machine Driven Workflow**: Applications follow strict, validated lifecycle transitions (`DRAFT` → `SUBMITTED` → `UNDER_REVIEW` → `APPROVED`/`REJECTED` → `ACCOUNT_CREATED`).
5. **Shared Kernel (`common-lib`)**: Provides common DTO wrappers (`ApiResponse`), centralized error handling, and reusable Spring Security/JWT filters across all modules.
6. **Declarative API Docs**: Integrated OpenAPI (Swagger UI) for interactive API discovery.
7. **Database Migration Safety**: Flyway versioned migrations enforce immutable database evolution.

---

## 🛠️ 3. Technology Stack

| Layer / Aspect | Technology | Version / Tool |
| :--- | :--- | :--- |
| **Language / Runtime** | Java | 21 (JDK 21) |
| **Framework** | Spring Boot | 3.3.x |
| **Build System** | Apache Maven | Multi-module Reactor (3.9.x) |
| **Inter-Service Sync**| Spring Cloud OpenFeign | 2023.0.3 |
| **Security** | Spring Security 6, JJWT | `io.jsonwebtoken:jjwt-api:0.12.5` |
| **Database** | PostgreSQL | 16-alpine (Docker Container) |
| **Schema Migrations**| Flyway | 10.x |
| **ORM / Persistence** | Spring Data JPA / Hibernate | Hibernate 6 |
| **Testing** | JUnit 5, Mockito | JUnit Jupiter 5.10.x |
| **Documentation** | Springdoc OpenAPI / Swagger | 2.5.0 |
| **Utilities** | Lombok, MapStruct | Lombok 1.18.x, MapStruct 1.5.5 |

---

## 📂 4. Project Structure & Directory Layout

```
account-opening-system/
├── pom.xml                        # Parent Multi-Module Maven POM (with Spring Cloud BOM)
├── README.md                      # High-level overview & 7-Day Roadmap
├── docker-compose-db.yml          # PostgreSQL 16 Docker Compose setup
├── PROJECT_CONTEXT.md             # Complete technical context & reference
│
├── common-lib/                    # Shared Kernel Module
│   ├── pom.xml
│   └── src/main/java/com/bank/common/
│       ├── dto/
│       │   └── ApiResponse.java   # Standardized JSON response wrapper
│       ├── exception/
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       └── security/
│           ├── JwtUtil.java       # JWT generation, validation & claims
│           └── JwtAuthenticationFilter.java # Spring Security Filter
│
├── authentication-service/        # Auth Service (Port 8081)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/bank/auth/
│       │   ├── config/SecurityConfig.java
│       │   ├── controller/AuthController.java
│       │   ├── dto/ (LoginRequest, RegisterRequest, TokenResponse, UserResponse)
│       │   ├── entity/ (User, RefreshToken)
│       │   ├── exception/ (UserAlreadyExistsException)
│       │   ├── repository/ (UserRepository, RefreshTokenRepository)
│       │   └── service/ (AuthService, CustomUserDetailsService)
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               ├── V1__create_users_table.sql
│               └── V2__create_refresh_tokens_table.sql
│
├── customer-service/              # Customer & KYC Domain Service (Port 8082)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/bank/customer/
│       │   ├── config/ (SecurityConfig.java, SwaggerConfig.java)
│       │   ├── controller/CustomerController.java
│       │   ├── dto/ (CreateCustomerRequest, CustomerResponse, UpdateKycRequest)
│       │   ├── entity/Customer.java (KycStatus enum: PENDING, VERIFIED, REJECTED)
│       │   ├── exception/ (CustomerAlreadyExistsException, CustomerNotFoundException, CustomerExceptionHandler)
│       │   ├── repository/CustomerRepository.java
│       │   ├── service/CustomerService.java
│       │   └── validation/ (ValidPan, PanValidator, ValidAadhaar, AadhaarValidator)
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               └── V1__create_customers_table.sql
│
└── account-opening-service/       # Application Orchestration & State Machine Service (Port 8083)
    ├── pom.xml
    └── src/main/
        ├── java/com/bank/account/
        │   ├── client/CustomerClient.java (OpenFeign Client to customer-service)
        │   ├── config/ (SecurityConfig.java, SwaggerConfig.java)
        │   ├── controller/AccountApplicationController.java
        │   ├── dto/ (CreateApplicationRequest, UpdateStatusRequest, ApplicationResponse, CustomerDto)
        │   ├── entity/ (AccountApplication, ApplicationStatus, AccountType)
        │   ├── exception/ (InvalidStateTransitionException, ApplicationNotFoundException, AccountExceptionHandler)
        │   ├── repository/AccountApplicationRepository.java
        │   └── service/AccountApplicationService.java (State Machine & Feign logic)
        └── resources/
            ├── application.yml
            └── db/migration/
                └── V1__create_account_applications_table.sql
```

---

## 🔍 5. Detailed Module Specifications

### 📦 A. `common-lib` (Shared Kernel)
* **Purpose**: Houses reusable cross-cutting concerns to eliminate code duplication across microservices.
* **Key Components**:
  * `ApiResponse<T>`: Standardized response envelope with fields: `success` (boolean), `message` (String), `data` (T), `timestamp` (Instant ISO-8601), and `correlationId`.
  * `JwtUtil`: Handles JWT HMAC-SHA key signing, parsing, claims extraction (subject, roles, userId), and token validation.
  * `JwtAuthenticationFilter`: Custom `OncePerRequestFilter` inspecting `Authorization: Bearer <token>` headers to populate `SecurityContextHolder`.
  * `GlobalExceptionHandler`: Centralized `@RestControllerAdvice` converting uncaught exceptions into clean `ApiResponse.error(...)` formats.

---

### 🔑 B. `authentication-service`
* **Port**: `8081`
* **Database**: `auth_db`
* **Purpose**: User identity management, security credential checking, JWT issuance, and refresh token rotation.
* **Core Endpoints**:
  * `POST /api/v1/auth/register` - Registers a new user with BCrypt password hashing.
  * `POST /api/v1/auth/login` - Authenticates credentials, generates Access Token (15-min TTL) & Refresh Token (7-day TTL).
  * `POST /api/v1/auth/refresh` - Issues a new access token via a valid, non-revoked refresh token.
  * `POST /api/v1/auth/logout` - Revokes refresh tokens.
* **Flyway Migrations**:
  * `V1__create_users_table.sql`: `users` (id UUID, username, email, password_hash, role, enabled, created_at).
  * `V2__create_refresh_tokens_table.sql`: `refresh_tokens` (id UUID, user_id UUID FK, token, expires_at, revoked).

---

### 👤 C. `customer-service`
* **Port**: `8082`
* **Database**: `customer_db`
* **Purpose**: Manages customer demographic profiles, address details, and KYC status transitions.
* **Key Business Rules**:
  * Enforces mandatory, format-validated Indian Tax & ID Identifiers (`@ValidPan`, `@ValidAadhaar`).
  * Uniqueness guarantees on `email`, `pan_number`, `aadhaar_number`, and `user_id`.
  * Role-Based Access Control (`@PreAuthorize`): Only `ROLE_ADMIN` can trigger KYC state updates (`VERIFIED` / `REJECTED`).
* **Core Endpoints**:
  * `POST /api/v1/customers` - Creates customer profile (KYC status set to `PENDING`).
  * `GET /api/v1/customers/{id}` - Retrieves customer by ID.
  * `GET /api/v1/customers/user/{userId}` - Retrieves customer profile by Auth User ID.
  * `PATCH /api/v1/customers/{id}/kyc` - Updates KYC status (`VERIFIED`/`REJECTED`) and remarks (Admin only).
* **Flyway Migrations**:
  * `V1__create_customers_table.sql`: `customer_db.customers` table with constraints and indexes on `user_id` and `kyc_status`.

---

### ⚙️ D. `account-opening-service` (Day 3 Implementation)
* **Port**: `8083`
* **Database**: `account_db`
* **Purpose**: Orchestrates state-machine driven account opening applications and integrates synchronously with `customer-service`.
* **State Machine Life Cycle**:
  * `DRAFT`: Initial draft creation with `applicationNumber` (`APP-XXXXXXXX`), customerId, accountType (`SAVINGS`, `CURRENT`, `SALARY`), and initial deposit.
  * `SUBMITTED`: Submitted by customer for bank processing.
  * `UNDER_REVIEW`: Admin begins application audit.
  * `APPROVED`: Admin approves application (Requires customer KYC status to be `VERIFIED` via Feign client).
  * `REJECTED`: Application rejected with mandatory remarks.
  * `ACCOUNT_CREATED`: Terminal status after account generation.
* **Core Endpoints**:
  * `POST /api/v1/applications` — Create draft application.
  * `POST /api/v1/applications/{id}/submit` — Submit application (`DRAFT` → `SUBMITTED`).
  * `PATCH /api/v1/applications/{id}/status` — Update application state (Admin action for review/approval).
  * `GET /api/v1/applications/{id}` — Retrieve application by ID.
  * `GET /api/v1/applications/customer/{customerId}` — Retrieve all applications for a customer.
  * `GET /api/v1/applications/user/{userId}` — Retrieve applications by User ID.
  * `GET /api/v1/applications` — List all applications (Admin only).
* **Flyway Migrations**:
  * `V1__create_account_applications_table.sql`: `account_db.account_applications` table with status and account type check constraints.

---

### 💰 E. `savings-account-service` (Day 4 Implementation)
* **Port**: `8084`
* **Database**: `savings_db`
* **Purpose**: Manages savings account creation with thread-safe account number generation using PostgreSQL sequence `savings_db.savings_account_seq`, balance operations, and account status management.
* **Key Features**:
  * Atomically generates non-sequential 10-digit account numbers (`1000000000 + nextval('savings_db.savings_account_seq')`).
  * Prevents duplicate account provisioning for the same application ID (`DuplicateAccountException`).
  * Supports credit/debit operations on balances (`CREDIT`, `DEBIT`) with `INSUFFICIENT_FUNDS` validation.
  * Synchronously provisioned by `account-opening-service` via OpenFeign upon application approval (`APPROVED` → `ACCOUNT_CREATED`).
* **Core Endpoints**:
  * `POST /api/v1/savings-accounts` — Create savings account (Internal/Feign/Admin).
  * `GET /api/v1/savings-accounts/{id}` — Retrieve account details by ID.
  * `GET /api/v1/savings-accounts/account-number/{accountNumber}` — Retrieve account details by Account Number.
  * `GET /api/v1/savings-accounts/customer/{customerId}` — Retrieve accounts for customer.
  * `GET /api/v1/savings-accounts/application/{applicationId}` — Retrieve account created for application.
  * `PATCH /api/v1/savings-accounts/{id}/status` — Update status (`ACTIVE`, `DORMANT`, `CLOSED`).
  * `POST /api/v1/savings-accounts/{id}/balance` — Credit or debit account balance.
* **Flyway Migrations**:
  * `V1__create_savings_accounts_table.sql`: `savings_db.savings_accounts` table and PostgreSQL sequence `savings_account_seq`.

---

## 🗄️ 6. Database Schema & Infrastructure

### PostgreSQL Docker Setup (`docker-compose-db.yml`)
* **Container Name**: `aos_postgres`
* **Image**: `postgres:16-alpine`
* **Host Port**: `5432`
* **Databases/Schemas**: `auth_db`, `customer_db`, `account_db`, `savings_db`
* **Credentials**: `postgres` / `password`

---

## 🔒 7. Security Architecture

1. **Password Protection**: Passwords stored using Spring Security's `BCryptPasswordEncoder`.
2. **Stateless JWT Authentication**:
   * Secret Key: HMAC SHA-256 (64-byte key configured in `application.yml`).
   * Access Token Expiration: 15 minutes (`900000 ms`).
   * Refresh Token Expiration: 7 days (`604800000 ms`).
3. **Role-Based Authorization (RBAC)**:
   * Roles: `ROLE_CUSTOMER`, `ROLE_ADMIN`.
   * Security Filters inspect JWT claims and populate Spring Security context (`GrantedAuthority`).

---

## 🏃 8. How to Run & Build

### Prerequisites
* JDK 21+
* Apache Maven 3.9+
* Docker & Docker Compose

### 1. Launch Database
```bash
docker compose -f docker-compose-db.yml up -d
```

### 2. Build All Modules
```bash
mvn clean install
```

### 3. Run Microservices
* **Authentication Service**:
  ```bash
  mvn -pl authentication-service spring-boot:run
  ```
* **Customer Service**:
  ```bash
  mvn -pl customer-service spring-boot:run
  ```
* **Account Opening Service**:
  ```bash
  mvn -pl account-opening-service spring-boot:run
  ```
* **Savings Account Service**:
  ```bash
  mvn -pl savings-account-service spring-boot:run
  ```

### 4. Interactive Swagger UI Documentation
* **Authentication Service API Docs**: `http://localhost:8081/swagger-ui.html`
* **Customer Service API Docs**: `http://localhost:8082/swagger-ui.html`
* **Account Opening Service API Docs**: `http://localhost:8083/swagger-ui.html`
* **Savings Account Service API Docs**: `http://localhost:8084/swagger-ui.html`

---

## 🗺️ 9. Roadmap & Implementation Status

- [x] **Day 1: Foundations**
  - Git repository & Maven multi-module reactor POM.
  - `common-lib` module with `ApiResponse`, JWT utilities, and `GlobalExceptionHandler`.
  - `authentication-service` with registration, login, token refresh, logout, and Flyway migrations V1 & V2.
- [x] **Day 2: Customer Service (`customer-service`)**
  - Custom PAN/Aadhaar validators (`@ValidPan`, `@ValidAadhaar`).
  - Customer CRUD operations, duplicate checks, and KYC status state management.
  - Security integration & unit tests (`CustomerServiceTest`).
- [x] **Day 3: Application Orchestration (`account-opening-service`)**
  - Built `account-opening-service` module (Port 8083).
  - Explicit State Machine workflow (`DRAFT` → `SUBMITTED` → `UNDER_REVIEW` → `APPROVED`/`REJECTED` → `ACCOUNT_CREATED`).
  - Integrated Spring Cloud OpenFeign (`CustomerClient`) for synchronous customer existence & `VERIFIED` KYC validation.
  - Flyway migration `V1__create_account_applications_table.sql`.
  - Comprehensive unit tests (`AccountApplicationServiceTest`).
- [x] **Day 4: Savings Account Generation (`savings-account-service`)**
  - Built `savings-account-service` module (Port 8084).
  - Thread-safe, sequence-backed 10-digit account number generation.
  - Balance operations (credit/debit) and status management.
  - OpenFeign integration in `account-opening-service` for auto-provisioning on application approval (`APPROVED` → `ACCOUNT_CREATED`).
  - Flyway migration `V1__create_savings_accounts_table.sql`.
  - Comprehensive unit tests (`SavingsAccountServiceTest`).
- [ ] **Day 5: Containerization & Docker Compose**
- [ ] **Day 6: CI/CD Pipeline & Kubernetes Manifests**
- [ ] **Day 7: AWS EKS Deployment**
