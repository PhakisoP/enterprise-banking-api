# Enterprise Banking API

A junior portfolio project demonstrating a Spring Boot REST API for a simple banking application.

The API provides account management and financial transaction capabilities backed by MySQL, with transaction management, optimistic locking, request validation, consistent HTTP error responses, database migrations, automated integration testing, and OpenAPI documentation.

---

## Overview

The Enterprise Banking API is the backend service for a full-stack banking application.

It currently provides REST endpoints for:

* Retrieving account information
* Retrieving transaction history
* Depositing funds
* Withdrawing funds
* Transferring funds between accounts

Financial operations use Spring transaction management so related database changes succeed or roll back together.

---

## Key Features

* Account balance retrieval
* Transaction history
* Deposits
* Withdrawals
* Account-to-account transfers
* Atomic transfer processing
* Transaction rollback for failed operations
* Request validation
* Global exception handling
* RFC 9457-style `ProblemDetail` error responses
* Optimistic locking for concurrent account updates
* MySQL persistence
* Flyway database migrations
* H2-based automated tests
* OpenAPI / Swagger documentation
* CORS configuration through environment variables
* Maven-based build and test lifecycle

---

## Technology Stack

| Technology        | Purpose                          |
| ----------------- | -------------------------------- |
| Java 25           | Application language and runtime |
| Spring Boot 4.1.1 | Application framework            |
| Spring MVC        | REST API                         |
| Spring Data JPA   | Persistence abstraction          |
| Hibernate         | ORM                              |
| MySQL             | Application database             |
| Flyway            | Database migrations              |
| H2                | Test database                    |
| Maven             | Build and dependency management  |
| SpringDoc OpenAPI | API documentation                |
| JUnit             | Automated testing                |

---

## Architecture

The application follows a layered architecture:

```text
HTTP Request
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
MySQL Database
```

### Controller layer

Handles HTTP requests, request validation, API documentation, and HTTP responses.

### Service layer

Contains banking business logic and transaction boundaries.

### Repository layer

Provides persistence through Spring Data JPA.

## Database Architecture

The API maps account and transaction records to MySQL tables. Accounts include a customer ID value and an optimistic-locking version column; there is no separate Customer entity.

The current schema has two tables. The diagram lists representative columns; `customer_id` and `transactions.account_number` are values in the current migration, not declared foreign keys.

```mermaid
erDiagram
    accounts {
        INT account_number PK
        INT customer_id
        VARCHAR account_type
        DECIMAL balance
        BIGINT version
        VARCHAR pin
        INT failed_attempts
        BIT is_locked
    }
    transactions {
        INT transaction_id PK
        INT account_number
        VARCHAR transaction_type
        DECIMAL amount
        DECIMAL balance_after
        DATETIME transaction_date
    }
```

---

## Project Structure

```text
enterprise-banking-api/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/phakiso/enterprisebankingapi/
│   │   │       ├── account/
│   │   │       ├── config/
│   │   │       ├── exception/
│   │   │       └── transaction/
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/
│   │           └── migration/
│   │
│   └── test/
│       ├── java/
│       │   └── com/phakiso/enterprisebankingapi/
│       └── resources/
│           └── application-test.yaml
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

---

## REST API

Base URL:

```text
http://localhost:8080
```

### Account

Retrieve account details:

```http
GET /api/v1/accounts/{accountNumber}
```

Example:

```http
GET /api/v1/accounts/888888
```

Example response:

```json
{
  "accountNumber": 888888,
  "customerId": 888888,
  "accountType": "Savings",
  "balance": 49000.00
}
```

---

### Deposit

```http
POST /api/v1/accounts/{accountNumber}/deposits
```

Example request:

```json
{
  "amount": 1000.00
}
```

---

### Withdrawal

```http
POST /api/v1/accounts/{accountNumber}/withdrawals
```

Example request:

```json
{
  "amount": 500.00
}
```

---

### Transfer

```http
POST /api/v1/accounts/{accountNumber}/transfers
```

Example request:

```json
{
  "destinationAccountNumber": 999999,
  "amount": 1000.00
}
```

Transfers are processed atomically. The source account, destination account, and transaction records are handled within the same transaction boundary.

---

### Transaction History

```http
GET /api/v1/accounts/{accountNumber}/transactions
```

Returns transaction history associated with an account.

---

## Error Handling

The API uses a centralized exception-handling mechanism.

Errors are returned using Spring's `ProblemDetail` representation where appropriate.

Typical HTTP responses include:

| Status | Meaning                                            |
| ------ | -------------------------------------------------- |
| `200`  | Operation completed successfully                   |
| `400`  | Invalid request or validation failure              |
| `404`  | Account or resource does not exist                 |
| `409`  | Concurrent account modification                    |
| `422`  | Business rule violation such as insufficient funds |

---

## Optimistic Locking

Account updates use optimistic locking to protect against concurrent modifications.

The account entity maintains a version value which allows the application to detect when another transaction has modified the same account before an update is completed.

A concurrent modification results in an appropriate conflict response rather than silently overwriting another transaction's changes.

---

## Database

The application uses MySQL.

Database schema changes are managed through Flyway migrations located under:

```text
src/main/resources/db/migration/
```

Migrations run in order on a new empty database:

```text
V1__create_account_tables.sql
V2__add_account_version.sql
```

V1 creates the `accounts` and `transactions` tables. V2 adds the account version used for optimistic locking. The existing `baseline-on-migrate` setting supports databases whose tables were created before Flyway history was introduced. Automated tests use an isolated H2 schema created by Hibernate and do not run Flyway migrations.

## Security Scope

Database credentials are supplied through environment variables. CORS allows one configured frontend origin, and Actuator exposes only health information. The API does not implement login or authorization; authentication is intentionally outside this junior portfolio project's scope. This junior portfolio project is not a production banking service.

---

## Configuration

Application configuration is located in:

```text
src/main/resources/application.yaml
```

Test-specific configuration is located in:

```text
src/test/resources/application-test.yaml
```

Environment-specific values should be supplied through environment variables rather than committed credentials:

| Variable | Required | Default | Purpose |
| --- | --- | --- | --- |
| DB_PASSWORD | Yes for a password-protected MySQL account | None | MySQL account password |
| DB_URL | No | jdbc:mysql://localhost:3306/enterprise_banking | JDBC connection URL |
| DB_USERNAME | No | root | MySQL account name |
| APP_CORS_ALLOWED_ORIGIN | No | http://localhost:5173 | Allowed frontend origin |

Never put a real password in source control or shell history. Set DB_PASSWORD in your local environment or secret manager. The .gitignore excludes local .env files; .env.example may contain placeholders only.
---

## OpenAPI Documentation

The API includes OpenAPI documentation through SpringDoc.

When the application is running, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

The generated OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

These endpoints can be used to explore and test the API interactively.

---

## Running the Application

### Requirements

* Java 25
* MySQL
* The included Maven wrapper (a separate Maven installation is not required)

Create the database before starting the application. In your MySQL client, run:

    CREATE DATABASE enterprise_banking;

Set DB_PASSWORD in your local environment to the MySQL account password. The default connection uses root on localhost; set DB_USERNAME and/or DB_URL if your setup differs. Use placeholders only in examples; never commit a real password.

### Start the application

From the `enterprise-banking-api` directory:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will start on:

```text
http://localhost:8080
```

Flyway creates the schema but does not add demo accounts. After the first successful startup, add local sample records if you want to try the account endpoints and frontend:

```sql
INSERT INTO accounts (account_number, customer_id, account_type, balance)
VALUES
    (888888, 888888, 'Cheque', 31000.00),
    (999999, 999999, 'Savings', 2000.00);
```

---

## Verification

### Automated tests and full build

Automated tests use the test profile's in-memory H2 database. Hibernate creates and drops that isolated schema; Flyway is disabled in this profile. These tests are separate from the completed manual MySQL verification below.

From the enterprise-banking-api directory, run:

    .\mvnw.cmd test

This runs the automated test phase. Run the full Maven verification lifecycle, including tests and package checks, with:

    .\mvnw.cmd verify

Both commands should finish with BUILD SUCCESS.

## Completed Manual MySQL Verification

Phase 8/9 manual verification is complete and accepted. The recorded evidence covers a completely fresh MySQL database, Spring Boot startup, Flyway V1 then V2, JPA schema validation, successful application startup, and `/actuator/health` returning HTTP 200. This is separate from the automated H2 test profile and is not being repeated.

## API Verification

With the application running, account retrieval can be verified with:

```powershell
Invoke-WebRequest http://localhost:8080/api/v1/accounts/888888 -UseBasicParsing |
    Select-Object StatusCode, Content
```

Expected result:

```text
StatusCode: 200
```

Swagger UI can be verified with:

```powershell
Invoke-WebRequest http://localhost:8080/swagger-ui.html -UseBasicParsing |
    Select-Object StatusCode
```

OpenAPI can be verified with:

```powershell
Invoke-WebRequest http://localhost:8080/v3/api-docs -UseBasicParsing |
    Select-Object StatusCode
```

Both should return:

```text
200
```

---

## Testing Strategy

The project contains several layers of automated testing:

### Unit tests

Business logic is tested independently of the HTTP layer.

### Controller tests

REST controller behaviour, request handling, validation, and responses are tested.

### Integration tests

The application context and real HTTP behaviour are tested through isolated integration tests.

### Optimistic locking tests

Concurrent account modification scenarios are tested to verify conflict handling.

### OpenAPI integration test

The generated OpenAPI documentation is verified as part of the automated test suite.

---

## Development History

The project has evolved incrementally from a basic account API into a junior full-stack portfolio backend.

Major milestones include:

* Account persistence
* Account retrieval API
* Transaction persistence
* Transaction history
* Deposits
* Withdrawals
* Transaction rollback
* API validation
* Global exception handling
* Optimistic locking
* Transfer processing
* Integration testing
* OpenAPI documentation
* Externalized CORS configuration

---

## Current Status

The backend currently provides:

* RESTful account operations
* Transaction history
* Deposits
* Withdrawals
* Account transfers
* Transaction management
* Optimistic locking
* MySQL persistence
* Flyway migrations
* Global error handling
* OpenAPI documentation
* Automated testing
* Externalized CORS configuration

The API is being developed as the backend foundation for the Enterprise Banking full-stack application.

---

## Related Project

Frontend: [Enterprise Banking Web](https://github.com/PhakisoP/enterprise-banking-web)

The frontend is built with React and Vite and consumes this REST API.

---

## Author

**Phakiso**

Enterprise Banking API — full-stack banking application portfolio project.
