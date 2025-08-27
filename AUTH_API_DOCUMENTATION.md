# Authentication Service API Documentation

This document provides comprehensive documentation for the Authentication Service API endpoints.

## Base URL
```
http://localhost:8080/api/auth
```

## Authentication Flow Overview

1. **Register** → User registration with email verification
2. **Email Verification** → User verifies email via Keycloak
3. **Login** → Credentials validation + OTP sent to email
4. **OTP Verification** → OTP validation + Access/Refresh tokens returned
5. **Token Refresh** → Refresh access tokens when expired

---

## API Endpoints

### 1. User Registration

**Endpoint:** `POST /api/auth/register`

**Description:** Registers a new user and sends email verification

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "confirmedPassword": "string",
  "firstName": "string",
  "lastName": "string"
}
```

**Response:** `201 Created`
```json
{
  "id": "keycloak-user-id",
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "message": "User registered successfully. Please check your email for verification."
}
```

**Error Responses:**
- `400 Bad Request` - Passwords don't match or validation errors
- `409 Conflict` - User already exists
- `500 Internal Server Error` - Registration failed

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "confirmedPassword": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

---

### 2. User Login

**Endpoint:** `POST /api/auth/login`

**Description:** Validates credentials and sends OTP to verified email

**Request Body:**
```json
{
  "usernameOrEmail": "string",
  "password": "string"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": null,
  "refreshToken": null,
  "tokenType": "Bearer",
  "expiresIn": 0,
  "userId": "keycloak-user-id",
  "username": "john_doe",
  "email": "john@example.com",
  "emailVerified": true,
  "message": "OTP sent to your email. Please verify OTP to complete login."
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `403 Forbidden` - Email not verified
- `500 Internal Server Error` - Login failed

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "SecurePass123!"
  }'
```

---

### 3. Email Verification (Resend)

**Endpoint:** `POST /api/auth/verify/{userId}`

**Description:** Resends email verification to user

**Path Parameters:**
- `userId` (string) - Keycloak user ID

**Response:** `200 OK`
```json
"Verification email sent successfully"
```

**Error Responses:**
- `500 Internal Server Error` - Failed to send verification email

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/auth/verify/abc123-def456-ghi789 \
  -H "Content-Type: application/json"
```

---

### 4. Send OTP

**Endpoint:** `POST /api/auth/send-otp`

**Description:** Sends OTP to specified email for given purpose

**Request Body:**
```json
{
  "email": "string",
  "purpose": "string"
}
```

**Response:** `200 OK`
```json
{
  "message": "OTP sent successfully",
  "otpId": "123",
  "expiresIn": 300
}
```

**Error Responses:**
- `400 Bad Request` - Invalid email or purpose
- `500 Internal Server Error` - Failed to send OTP

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "purpose": "LOGIN"
  }'
```

---

### 5. Verify OTP

**Endpoint:** `POST /api/auth/verify-otp`

**Description:** Verifies OTP and returns access/refresh tokens

**Request Body:**
```json
{
  "email": "string",
  "otp": "string"
}
```

**Response:** `200 OK`
```json
{
  "verified": true,
  "message": "OTP verified successfully",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "keycloak-user-id"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid OTP, expired OTP, or max attempts exceeded
- `404 Not Found` - No valid OTP found
- `500 Internal Server Error` - Verification failed

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otp": "123456"
  }'
```

---

### 6. Refresh Token

**Endpoint:** `POST /api/auth/refresh`

**Description:** Refreshes access token using refresh token

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid or expired refresh token
- `500 Internal Server Error` - Token refresh failed

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

---

## Complete Authentication Flow Example

### Step 1: Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "confirmedPassword": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Step 2: User verifies email (via Keycloak email link)

### Step 3: Login (triggers OTP)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "SecurePass123!"
  }'
```

### Step 4: Verify OTP (get tokens)
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otp": "123456"
  }'
```

### Step 5: Use access token for authenticated requests
```bash
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Step 6: Refresh token when needed
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

---

## Configuration Requirements

### Email Configuration (application.yml)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### OTP Configuration (application.yml)
```yaml
app:
  otp:
    expiry-minutes: 5
    max-attempts: 3
```

---

## Security Features

- **Email Verification Required**: Users must verify email before login
- **OTP Verification**: Additional security layer with time-limited OTP
- **Token Expiration**: Access tokens expire in 1 hour
- **Refresh Tokens**: Long-lived tokens for seamless experience
- **Rate Limiting**: Maximum 3 OTP attempts per request
- **Automatic Cleanup**: Expired OTPs are automatically removed

---

## Error Handling

All endpoints return standardized error responses:

```json
{
  "error": "Error message",
  "timestamp": "2025-08-27T09:52:53.953572672",
  "status": 400
}
```

Common HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `500` - Internal Server Error

---

## Postman Collection

You can import this collection into Postman for easy testing:

```json
{
  "info": {
    "name": "Auth Service API",
    "description": "Authentication service endpoints"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api/auth"
    }
  ],
  "item": [
    {
      "name": "Register",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/register",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"password\": \"SecurePass123!\",\n  \"confirmedPassword\": \"SecurePass123!\",\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\"\n}"
        }
      }
    },
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/login",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"usernameOrEmail\": \"john_doe\",\n  \"password\": \"SecurePass123!\"\n}"
        }
      }
    },
    {
      "name": "Verify OTP",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/verify-otp",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"email\": \"john@example.com\",\n  \"otp\": \"123456\"\n}"
        }
      }
    },
    {
      "name": "Refresh Token",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/refresh",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "raw": "{\n  \"refreshToken\": \"your-refresh-token\"\n}"
        }
      }
    }
  ]
}
```
