# Authentication Service API Documentation

This document provides comprehensive documentation for the Authentication Service API endpoints.

## Base URL
```
http://localhost:8080/api/auth
```

## Authentication Flow Overview

### **New Authentication Flow:**
1. **Register** → User registration with email verification
2. **Email Verification** → User verifies email via Keycloak
3. **Login** → Direct token return (no OTP required)
4. **Token Refresh** → Refresh access tokens when expired

### **Forgot Password Flow:**
1. **Forgot Password** → Request password reset OTP
2. **Verify OTP** → Validate password reset OTP
3. **Reset Password** → Set new password with validated OTP

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

### 2. User Login (Direct Token Return)

**Endpoint:** `POST /api/auth/login`

**Description:** Validates credentials and returns access/refresh tokens directly

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "keycloak-user-id",
  "username": "john_doe",
  "email": "john@example.com",
  "emailVerified": true,
  "message": "Login successful"
}
```

### 3. Token Refresh

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

---

## Forgot Password Flow

### 4. Forgot Password

**Endpoint:** `POST /api/auth/forgot-password`

**Description:** Sends password reset OTP to user's email

**Request Body:**
```json
{
  "email": "string"
}
```

**Response:** `200 OK`
```json
{
  "message": "Password reset OTP sent successfully",
  "otpId": "123",
  "expiresIn": 300
}
```

### 5. Verify Forgot Password OTP

**Endpoint:** `POST /api/auth/verify-forgot-password-otp`

**Description:** Verifies password reset OTP (no tokens returned)

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
  "message": "Password reset OTP verified successfully. You can now reset your password.",
  "accessToken": null,
  "refreshToken": null,
  "userId": "keycloak-user-id"
}
```

### 6. Reset Password

**Endpoint:** `POST /api/auth/reset-password`

**Description:** Resets user password with validated OTP

**Request Body:**
```json
{
  "email": "string",
  "otp": "string",
  "newPassword": "string"
}
```

**Response:** `200 OK`
```json
"Password reset successfully"
```

---

## Legacy Endpoints (Backward Compatibility)

### 7. Send OTP (General Purpose)

**Endpoint:** `POST /api/auth/send-otp`

**Description:** Sends OTP for general purposes

**Request Body:**
```json
{
  "email": "string",
  "purpose": "string"
}
```

### 8. Verify OTP (General Purpose)

**Endpoint:** `POST /api/auth/verify-otp`

**Description:** Verifies general purpose OTP and returns tokens

**Request Body:**
```json
{
  "email": "string",
  "otp": "string"
}
```

### 9. Resend Email Verification

**Endpoint:** `POST /api/auth/verify/{userId}`

**Description:** Resends email verification to user

---

## Complete Flow Examples

### Standard Login Flow
```bash
# 1. Register
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

# 2. User verifies email via Keycloak link

# 3. Login and get tokens directly
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "SecurePass123!"
  }'

# 4. Use tokens for authenticated requests
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Forgot Password Flow
```bash
# 1. Request password reset OTP
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com"
  }'

# 2. Verify password reset OTP
curl -X POST http://localhost:8080/api/auth/verify-forgot-password-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otp": "123456"
  }'

# 3. Reset password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otp": "123456",
    "newPassword": "NewSecurePass123!"
  }'
```

---

## Key Changes from Previous Version

### **Login Flow Changes:**
- ✅ **Direct Token Return**: Login now returns access/refresh tokens immediately
- ❌ **No OTP Required**: Removed OTP requirement from login process
- ✅ **Email Verification Still Required**: Users must verify email before login

### **New Forgot Password Features:**
- ✅ **Dedicated Forgot Password Flow**: Separate OTP process for password reset
- ✅ **Password Reset OTP**: Uses "FORGOT_PASSWORD" purpose in database
- ✅ **Secure Password Reset**: OTP verification required before password change

### **Architecture Improvements:**
- ✅ **Clear Separation**: Login vs Password Reset flows are distinct
- ✅ **Backward Compatibility**: Legacy OTP endpoints still available
- ✅ **Enhanced Security**: Different OTP purposes prevent cross-contamination

---

## Security Features

- **Email Verification Required**: Users must verify email before login
- **Direct Token Access**: Faster login experience without additional OTP step
- **Secure Password Reset**: OTP-protected password reset functionality
- **Token Expiration**: Access tokens expire in 1 hour
- **Refresh Tokens**: Long-lived tokens for seamless experience
- **Rate Limiting**: Maximum 3 OTP attempts per request
- **Purpose-based OTP**: Different OTP types for different purposes

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
- `403` - Forbidden (Email not verified)
- `404` - Not Found
- `409` - Conflict
- `500` - Internal Server Error
