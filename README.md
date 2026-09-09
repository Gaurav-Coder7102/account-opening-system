# Banking Account Opening System

A microservices-based Banking Account Opening System built with Java 21 and Spring Boot 3.3.x. This project demonstrates a production-ready architecture designed to be containerized with Docker, orchestrated with Kubernetes, and deployed to AWS.

## Architecture Overview

The system consists of 4 core microservices communicating synchronously, sharing a single PostgreSQL instance with separate schemas per service (Database-per-Service principle). 

- **authentication-service**: Handles user registration, login, JWT token issuance and validation.
- **customer-service**: Manages customer profiles, KYC status, and handles business validation (e.g., duplicate checks).
- **account-opening-service**: Orchestrates the state-machine-driven account opening workflow (DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> ACCOUNT_CREATED).
- **savings-account-service**: Generates unique, thread-safe account numbers and manages savings account balances and statuses.

## Technology Stack
- **Language / Runtime**: Java 17+
- **Framework**: Spring Boot 3.3.x, Spring Cloud OpenFeign
- **Security**: Spring Security 6, JWT (jjwt), BCrypt
- **Persistence**: Spring Data JPA, Hibernate 6, PostgreSQL 16, Flyway 10
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven 3.9.x (Multi-module reactor)
- **API Docs**: Swagger UI (springdoc-openapi)
- **Utilities**: Lombok, MapStruct

---

## 7-Day Implementation Roadmap

### [x] Day 1: Foundations
- Initialized the Git repository and Maven multi-module reactor project.
- Created the `common-lib` for shared DTOs (like `ApiResponse`), Global Exception Handling, and JWT validation logic.
- Built the `authentication-service` with endpoints for registration, login, token refresh, and logout. 
- Integrated Spring Security, BCrypt password hashing, and Flyway database migrations for User and RefreshToken entities.

### [x] Day 2: Domain Service #1 (`customer-service`)
- Build the customer profile service with Jakarta Bean Validation.
- Implement strict business rules (e.g., rejecting duplicate PAN/Aadhaar/emails) and Role-Based Access Control (@PreAuthorize).

### [x] Day 3: Orchestration (`account-opening-service`)
- Build the core orchestration service with an explicit state machine for application transitions.
- Wire up `OpenFeign` to communicate synchronously with the `customer-service`.

### [x] Day 4: Domain Service #2 (`savings-account-service`)
- Build the savings account generation service.
- Implement a thread-safe, non-sequential account number generator using database sequences.
- Wire `account-opening-service` to call `savings-account-service` on approval.

### [x] Day 5: Dockerization
- Write multi-stage Dockerfiles for all 4 services (`authentication-service`, `customer-service`, `account-opening-service`, `savings-account-service`).
- Implement automated PostgreSQL multi-database init script (`init-db/01-init-databases.sql`).
- Create a comprehensive root `docker-compose.yml` orchestrating all 4 microservices + PostgreSQL with health checks, environment configuration, and service networking.

### [ ] Day 6: CI/CD & Kubernetes
- Build a GitHub Actions CI/CD pipeline to build, test, and push Docker images.
- Write Kubernetes manifests (`Deployment`, `Service`, `ConfigMap`, `Secret`, `Ingress`).
- Deploy to a local `kind` cluster.

### [ ] Day 7: AWS Deployment
- Push Docker images to Amazon ECR.
- Provision an Amazon EKS cluster using `eksctl`.
- Deploy Kubernetes manifests to EKS and expose the system via a public LoadBalancer.

---

## How to Run Locally

### Option A: Complete Ecosystem with Docker Compose (Recommended)
Start the entire stack (PostgreSQL + all 4 microservices):
```bash
docker compose up -d --build
```

Check running services & health:
```bash
docker compose ps
```

To view logs across all containers:
```bash
docker compose logs -f
```

To stop all services:
```bash
docker compose down
```

### Option B: Run Standalone Database + Local Spring Boot Services
1. **Start the Database**:
   ```bash
   docker compose -f docker-compose-db.yml up -d
   ```
2. **Build the Project**:
   ```bash
   mvn clean install
   ```
3. **Run Services Individually**:
   ```bash
   mvn -pl authentication-service spring-boot:run
   mvn -pl customer-service spring-boot:run
   mvn -pl account-opening-service spring-boot:run
   mvn -pl savings-account-service spring-boot:run
   ```

### 4. Service Endpoints & Swagger UI
| Microservice | Port | Actuator Health | Swagger UI |
| :--- | :--- | :--- | :--- |
| **authentication-service** | `8081` | `http://localhost:8081/actuator/health` | `http://localhost:8081/swagger-ui.html` |
| **customer-service** | `8082` | `http://localhost:8082/actuator/health` | `http://localhost:8082/swagger-ui.html` |
| **account-opening-service** | `8083` | `http://localhost:8083/actuator/health` | `http://localhost:8083/swagger-ui.html` |
| **savings-account-service** | `8084` | `http://localhost:8084/actuator/health` | `http://localhost:8084/swagger-ui.html` |

