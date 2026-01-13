# SimpleBankAPI

A robust transaction processing system with ACID guarantees and advanced concurrency control for banking operations.

## 🎯 Technical Highlights

- **Pessimistic Locking** with deadlock prevention (ordered lock acquisition)
- **SERIALIZABLE Isolation** level for strict data consistency
- **Idempotency** using unique transaction references
- **Retry Logic** for handling transient deadlocks in high-concurrency scenarios
- **Integration Tests** with H2 in-memory database
- **DTO Pattern** for clean API contracts
- **Global Exception Handling** with proper HTTP status codes

## 🏗️ Architecture

### Layered Architecture
```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

### Key Components
- **Controllers**: RESTful endpoints with request validation
- **Services**: Transaction management with concurrency control
- **DTOs**: Request/Response objects (DepositRequest, WithdrawalRequest, TransferRequest)
- **Entities**: JPA models (Account, Transaction) with versioning
- **Repositories**: Spring Data JPA with custom locking queries
- **Exception Handlers**: Centralized error handling

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
  - Spring Data JPA
  - Spring Web
  - Spring DevTools
- **PostgreSQL 16** (Production)
- **H2 Database** (Testing)
- **JUnit 5** + Spring Boot Test
- **Docker Compose** for database orchestration
- **Maven** for build management

## 📦 Setup & Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd SimpleBankAPI
```

### 2. Start PostgreSQL Database
```bash
docker-compose up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `bankdb`
- Username: `user`
- Password: `pass`

### 3. Build the Project
```bash
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

## 🧪 Running Tests

Run all tests (uses H2 in-memory database):
```bash
mvn test
```

Run specific test:
```bash
mvn test -Dtest=TransactionServiceTest
```

### Test Coverage
- ✅ Deposit idempotency
- ✅ Withdrawal idempotency
- ✅ Insufficient funds validation
- ✅ Negative amount validation
- ✅ Concurrent transfers with balance consistency

## 🔌 API Endpoints

### Account Management

#### Create Account
```http
POST /accounts/new
Content-Type: application/json

{
  "numberAccount": 1234567890,
  "ownerAccount": "John Doe",
  "balance": 1000.00
}
```

#### Get All Transactions
```http
GET /accounts/{id}/transactions
```

### Transaction Operations

#### Deposit
```http
POST /accounts/{id}/deposit
Content-Type: application/json

{
  "transactionRef": "DEP-2025-001",
  "amount": 500.00
}
```

#### Withdrawal
```http
POST /accounts/{id}/withdrawal
Content-Type: application/json

{
  "transactionRef": "WITH-2025-001",
  "amount": 200.00
}
```

#### Transfer
```http
POST /accounts/transfer
Content-Type: application/json

{
  "fromId": "uuid-of-sender",
  "toId": "uuid-of-receiver",
  "transactionRef": "TRF-2025-001",
  "amount": 1000.00
}
```

**Transfer Limits:**
- Maximum per transaction: 5,000
- Maximum per day: 5,000

### Transaction History

#### Get Transaction History with Date Filters
```http
GET /accounts/{id}/history?startDate=2025-01-01T00:00:00&finishDate=2025-01-31T23:59:59
```

### Maintenance

#### Recalculate Balance
```http
POST /accounts/{id}/recalculation
```

Recalculates account balance from transaction history and updates if different.

## 🔐 Concurrency Features

### Deadlock Prevention
The system uses **ordered lock acquisition** to prevent deadlocks during transfers:
```java
// Locks are acquired in a consistent order based on account IDs
if (fromId.compareTo(toId) < 0) {
    lock(fromAccount);
    lock(toAccount);
} else {
    lock(toAccount);
    lock(fromAccount);
}
```

### Transaction Isolation
- Uses `SERIALIZABLE` isolation level for transfers
- Ensures strict consistency in high-concurrency scenarios
- Automatic retry logic handles transient serialization failures

### Idempotency
All operations are idempotent using unique `transactionRef`:
- Duplicate requests with same `transactionRef` return existing transaction
- Prevents double-processing in case of retries
- `transactionRef` must be unique per account

## 📊 Database Schema

### Accounts Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | Primary Key |
| number_account | BIGINT | - |
| owner_account | VARCHAR | - |
| balance | DECIMAL | - |
| created_at | TIMESTAMP | - |
| version | BIGINT | Optimistic locking |

### Transactions Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | Primary Key |
| transaction_ref | VARCHAR | - |
| debit | DECIMAL | Nullable |
| credit | DECIMAL | Nullable |
| date | TIMESTAMP | - |
| account_id | UUID | Foreign Key → accounts(id) |

## ⚠️ Exception Handling

The API returns appropriate HTTP status codes:

| Exception | Status Code | Description |
|-----------|-------------|-------------|
| `AccountNotFoundException` | 404 | Account does not exist |
| `InvalidAmountException` | 400 | Amount is zero or negative |
| `NotEnoughMoneyException` | 400 | Insufficient balance |
| `LimitReachedException` | 400 | Transfer limit exceeded |
| `TransactionRefDuplicationException` | 400 | Duplicate transactionRef across different accounts |

## 🔄 Configuration

### Application Properties

**Production** (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankdb
    username: user
    password: pass
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**Testing** (`application-test.yml`):
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## 📈 Performance Considerations

- **Optimistic Locking**: Account entity uses `@Version` for concurrent updates
- **Pessimistic Locking**: Critical sections use `SELECT FOR UPDATE`
- **Connection Pooling**: HikariCP for database connections
- **Batch Processing**: Consider implementing batch operations for high-volume scenarios

## 🚀 Future Enhancements

- [ ] Add Swagger/OpenAPI documentation
- [ ] Implement authentication & authorization (Spring Security)
- [ ] Add audit logging for compliance
- [ ] Implement pagination for transaction history
- [ ] Add account types (Checking, Savings, Business)
- [ ] Support multi-currency transactions
- [ ] Scheduled/recurring transactions
- [ ] Email/SMS notifications
- [ ] Transaction categorization and budgeting
- [ ] RESTful API versioning

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker-compose ps

# View database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Test Failures
```bash
# Clean build
mvn clean test

# Run with debug logging
mvn test -X
```

---

**Note**: This is a study project created to demonstrate understanding of Spring Boot, transaction management, concurrency control, and REST API design.
