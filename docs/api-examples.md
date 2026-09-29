# API Examples

## Base URL

```text
http://localhost:8080
```

All API endpoints use the `/api/v1` prefix.

## 1. Get Account Details

Returns the public details and current balance for an account.

**Request**

```http
GET /api/v1/accounts/1001
```

**Example**

```powershell
Invoke-WebRequest http://localhost:8080/api/v1/accounts/1001 -UseBasicParsing
```

**Example response**

```json
{
  "accountNumber": 1001,
  "customerId": 1,
  "accountType": "Cheque",
  "balance": 125.50
}
```

## 2. Get Transaction History

Returns the account's transactions ordered from newest to oldest.

**Request**

```http
GET /api/v1/accounts/1001/transactions
```

**Example response**

```json
[
  {
    "transactionId": 501,
    "accountNumber": 1001,
    "transactionType": "Deposit",
    "amount": 25.50,
    "balanceAfter": 125.50,
    "transactionDate": "2026-09-27T12:30:00"
  }
]
```

## 3. Deposit Funds

Adds a positive amount to an account and records the resulting transaction.

**Request**

```http
POST /api/v1/accounts/1001/deposits
Content-Type: application/json
```

**Request body**

```json
{
  "amount": 25.50
}
```

**Example**

```powershell
$body = @{
    amount = 25.50
} | ConvertTo-Json

Invoke-WebRequest `
    -Uri http://localhost:8080/api/v1/accounts/1001/deposits `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -UseBasicParsing
```

**Successful response**

```text
HTTP 200 OK
```

The operation records the resulting transaction. Retrieve the updated balance using the account details endpoint.

## 4. Withdraw Funds

Subtracts a positive amount from an account and records the resulting transaction.

**Request**

```http
POST /api/v1/accounts/1001/withdrawals
Content-Type: application/json
```

**Request body**

```json
{
  "amount": 25.50
}
```

**Example**

```powershell
$body = @{
    amount = 25.50
} | ConvertTo-Json

Invoke-WebRequest `
    -Uri http://localhost:8080/api/v1/accounts/1001/withdrawals `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -UseBasicParsing
```

**Successful response**

```text
HTTP 200 OK
```

Retrieve the updated balance using the account details endpoint.

## 5. Transfer Funds

Transfers funds from one account to another and records both sides of the transaction atomically.

**Request**

```http
POST /api/v1/accounts/1001/transfers
Content-Type: application/json
```

**Request body**

```json
{
  "destinationAccountNumber": 1002,
  "amount": 50.00
}
```

**Example**

```powershell
$body = @{
    destinationAccountNumber = 1002
    amount = 50.00
} | ConvertTo-Json

Invoke-WebRequest `
    -Uri http://localhost:8080/api/v1/accounts/1001/transfers `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -UseBasicParsing
```

**Successful response**

```text
HTTP 200 OK
```

The transfer records a `Transfer Out` transaction for the source account and a `Transfer In` transaction for the destination account.

## Error Responses

The API uses `application/problem+json` for documented error responses.

| HTTP status | Meaning |
| --- | --- |
| 400 Bad Request | Request validation failed or the transfer destination is invalid |
| 404 Not Found | The requested account does not exist |
| 409 Conflict | An account was concurrently modified |
| 422 Unprocessable Entity | The account has insufficient funds |

**Example error**

```json
{
  "type": "about:blank",
  "title": "Account Not Found",
  "status": 404,
  "detail": "Account 9999 does not exist."
}
```

The exact ProblemDetail fields and error messages depend on the exception being handled. This example illustrates the documented response format; its detail text is illustrative and is not guaranteed to match the runtime message.

## API Documentation

Interactive OpenAPI documentation is available during local development at:

<http://localhost:8080/swagger-ui/index.html>

The generated OpenAPI specification is available at:

<http://localhost:8080/v3/api-docs>

These URLs follow the current Springdoc setup. They have not been verified against a running application in this session.
