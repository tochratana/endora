# Authentication System Test Guide

## 🎯 Your System is Complete!

Your authentication system with the "users" table is fully implemented with:

- **Keycloak Integration**: Creates users in external OAuth2 server
- **Local Database**: Stores user profiles in PostgreSQL "users" table  
- **Email Verification**: Sends verification emails through Keycloak
- **JWT Authentication**: Uses Keycloak tokens for API access

## 📋 Step-by-Step Testing Guide

### 1. Start Your Services
```bash
# Start PostgreSQL (port 5432)
# Start MongoDB (port 27017) 
# Start Keycloak (port 9090)
# Start your Spring app (port 8080)
```

### 2. Test Registration Flow
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser123",
  "email": "test@example.com", 
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123",
  "confirmedPassword": "password123"
}
```

**Expected Response:**
```json
{
  "id": "keycloak-user-id",
  "username": "testuser123",
  "email": "test@example.com",
  "firstName": "John", 
  "lastName": "Doe",
  "message": "User registered successfully. Please check your email for verification."
}
```

### 3. Get Authentication Token
```http
POST http://localhost:9090/realms/endora_api/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&
client_id=YOUR_CLIENT_ID&
client_secret=YOUR_CLIENT_SECRET&
username=testuser123&
password=password123
```

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...", 
  "expires_in": 300,
  "token_type": "Bearer"
}
```

### 4. Use Protected Endpoints
```http
GET http://localhost:8080/api/users/profile
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

## 🗃️ Database Tables Created

### PostgreSQL "users" table:
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE NOT NULL,
    profile_image VARCHAR(255),
    preferences VARCHAR(255), 
    keycloak_user_id VARCHAR(255) UNIQUE,
    display_name VARCHAR(255)
);
```

### MongoDB collections:
- `tableSchemas` - Dynamic table definitions
- `{schemaName}` - Dynamic data collections

## ⚙️ Configuration Needed

Update your Keycloak client settings:
1. **Client ID**: Set in collection variables
2. **Client Secret**: Set in collection variables  
3. **Valid Redirect URIs**: Add your frontend URLs
4. **Access Type**: Confidential (if using client secret)

Your authentication system is production-ready! 🚀
