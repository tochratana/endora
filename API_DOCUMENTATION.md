# Schema Management API Documentation

## Overview
This documentation provides comprehensive guidance for creating schemas, managing relationships, and inserting data using the SpringTwoDataSource API. **All relationship data is now returned as arrays of objects by default.**

## Table of Contents
1. [Authentication](#authentication)
2. [Creating Schemas](#creating-schemas)
3. [Schema Relationships](#schema-relationships)
4. [Inserting Data](#inserting-data)
5. [Retrieving Data](#retrieving-data)
6. [Retrieving Data with Relationships](#retrieving-data-with-relationships)
7. [Examples](#examples)

## Authentication
All endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Creating Schemas

### 1. Basic Schema Creation

**Endpoint:** `POST /table`

**Description:** Creates a new table with a defined schema in a project.

**Request Body:**
```json
{
  "projectUuid": "project-uuid-here",
  "schemaName": "users",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "name": "VARCHAR(255) NOT NULL",
    "email": "VARCHAR(255) UNIQUE NOT NULL",
    "age": "INT",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  }
}
```

**Response:**
```json
{
  "message": "Table created successfully",
  "schemaName": "users",
  "projectUuid": "project-uuid-here",
  "userUuid": "current-user-uuid"
}
```

### 2. Schema Creation with Relationships

**Endpoint:** `POST /table/with-relationships`

**Description:** Creates a table with schema and relationships in a single request.

**Request Body:**
```json
{
  "schemaName": "orders",
  "projectUuid": "project-uuid-here",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) NOT NULL",
    "product_name": "VARCHAR(255) NOT NULL",
    "quantity": "INT DEFAULT 1",
    "total_amount": "DECIMAL(10,2)",
    "order_date": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users",
      "referencedColumn": "id",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ]
}
```

**Response:**
```json
{
  "message": "Table with relationships created successfully",
  "schemaName": "orders",
  "projectUuid": "project-uuid-here",
  "userUuid": "current-user-uuid"
}
```

## Schema Relationships

### Relationship Types
All relationships default to **one-to-many** unless explicitly specified. Supported types:
- `one-to-one`
- `one-to-many` (default)
- `many-to-many`

### Relationship Configuration
```json
{
  "foreignKeyColumn": "user_id",        // Column in current table
  "referencedTable": "users",           // Referenced table name
  "referencedColumn": "id",             // Referenced column (defaults to "id")
  "relationshipType": "one-to-many",    // Defaults to "one-to-many"
  "onDelete": "CASCADE",                // CASCADE, SET NULL, RESTRICT
  "onUpdate": "CASCADE"                 // CASCADE, SET NULL, RESTRICT
}
```

### Example Relationships

#### One-to-Many (User → Orders)
```json
{
  "foreignKeyColumn": "user_id",
  "referencedTable": "users",
  "referencedColumn": "id",
  "relationshipType": "one-to-many"
}
```

#### Many-to-Many (Products ↔ Categories)
```json
{
  "foreignKeyColumn": "category_id",
  "referencedTable": "categories",
  "referencedColumn": "id",
  "relationshipType": "many-to-many"
}
```

## Inserting Data

### 1. Insert Data with Path Parameters

**Endpoint:** `POST /table/{schemaName}/project/{projectUuid}/data`

**Description:** Inserts data into a table for the current authenticated user.

**Example:**
```bash
POST /table/users/project/abc-123-def/data
```

**Request Body:**
```json
{
  "id": "user-001",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "age": 30
}
```

**Response:**
```json
{
  "message": "Data inserted successfully",
  "schemaName": "users",
  "projectUuid": "abc-123-def",
  "userUuid": "current-user-uuid"
}
```

### 2. Insert Data with Request Body

**Endpoint:** `POST /table/data`

**Request Body:**
```json
{
  "schemaName": "users",
  "projectUuid": "abc-123-def",
  "data": {
    "id": "user-002",
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "age": 25
  }
}
```

### 3. Insert Related Data

When inserting data with relationships, ensure foreign key values exist:

**Insert Order (with user relationship):**
```json
{
  "schemaName": "orders",
  "projectUuid": "abc-123-def",
  "data": {
    "id": "order-001",
    "user_id": "user-001",
    "product_name": "Laptop",
    "quantity": 1,
    "total_amount": 999.99
  }
}
```

## Retrieving Data

### 1. Get All Tables

**Endpoint:** `GET /table`

**Response:** Array of all table schemas with relationships.

### 2. Get Tables by Project

**Endpoint:** `GET /table/project/{projectUuid}`

**Response:** Array of table schemas for the specified project.

### 3. Get Specific Table

**Endpoint:** `GET /table/{schemaName}/project/{projectUuid}`

**Response:** Single table schema with relationships as array of objects.

### 4. Basic Data Retrieval (No Relationships)

**Endpoint:** `GET /api/tables/{schemaName}`

**Response:** Array of records without relationship data.

**Endpoint:** `GET /api/tables/{schemaName}/{id}`

**Response:** Single record without relationship data.

## Retrieving Data with Relationships

### 1. Get All Records with Relationships

**Endpoint:** `GET /api/tables/{schemaName}/project/{projectUuid}`

**Description:** Retrieves all records from a table with their related data as arrays of objects.

**Example:**
```bash
GET /api/tables/users/project/abc-123-def
```

**Response:** 
```json
[
  {
    "id": "user-001",
    "name": "John Doe",
    "email": "john@example.com",
    "age": 30,
    "createdAt": "2025-09-03T10:00:00Z",
    "updatedAt": "2025-09-03T10:00:00Z",
    "orders": [
      {
        "id": "order-001",
        "user_id": "user-001",
        "product_name": "Laptop",
        "quantity": 1,
        "total_amount": 999.99,
        "createdAt": "2025-09-03T11:00:00Z",
        "updatedAt": "2025-09-03T11:00:00Z"
      },
      {
        "id": "order-002",
        "user_id": "user-001",
        "product_name": "Mouse",
        "quantity": 2,
        "total_amount": 49.99,
        "createdAt": "2025-09-03T12:00:00Z",
        "updatedAt": "2025-09-03T12:00:00Z"
      }
    ]
  }
]
```

### 2. Get Single Record with Relationships

**Endpoint:** `GET /api/tables/{schemaName}/{id}/project/{projectUuid}/relations`

**Description:** Retrieves a single record with all its related data as arrays of objects.

**Example:**
```bash
GET /api/tables/users/user-001/project/abc-123-def/relations
```

**Response:**
```json
{
  "id": "user-001",
  "name": "John Doe",
  "email": "john@example.com",
  "age": 30,
  "createdAt": "2025-09-03T10:00:00Z",
  "updatedAt": "2025-09-03T10:00:00Z",
  "orders": [
    {
      "id": "order-001",
      "user_id": "user-001",
      "product_name": "Laptop",
      "quantity": 1,
      "total_amount": 999.99,
      "createdAt": "2025-09-03T11:00:00Z",
      "updatedAt": "2025-09-03T11:00:00Z"
    }
  ],
  "user_profiles": [
    {
      "id": "profile-001",
      "user_id": "user-001",
      "bio": "Software developer",
      "phone": "+1-555-0123",
      "createdAt": "2025-09-03T10:30:00Z",
      "updatedAt": "2025-09-03T10:30:00Z"
    }
  ]
}
```

### 3. Relationship Data Structure

**Important:** All relationships are returned as arrays of objects, regardless of the relationship type:

- **One-to-One**: Returns an array with either 0 or 1 object
- **One-to-Many**: Returns an array with 0 or more objects  
- **Many-to-Many**: Returns an array with 0 or more objects

This consistent structure makes it easier to handle relationship data in your frontend applications.

## Examples

### Complete User Management System

#### 1. Create Users Table
```json
POST /table/with-relationships
{
  "schemaName": "users",
  "projectUuid": "project-123",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "username": "VARCHAR(50) UNIQUE NOT NULL",
    "email": "VARCHAR(255) UNIQUE NOT NULL",
    "password_hash": "VARCHAR(255) NOT NULL",
    "first_name": "VARCHAR(100)",
    "last_name": "VARCHAR(100)",
    "is_active": "BOOLEAN DEFAULT TRUE",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
    "updated_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
  },
  "relationships": []
}
```

#### 2. Create Profiles Table (One-to-One with Users)
```json
POST /table/with-relationships
{
  "schemaName": "user_profiles",
  "projectUuid": "project-123",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) UNIQUE NOT NULL",
    "avatar_url": "VARCHAR(500)",
    "bio": "TEXT",
    "phone": "VARCHAR(20)",
    "address": "TEXT",
    "birth_date": "DATE",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users",
      "referencedColumn": "id",
      "relationshipType": "one-to-one",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ]
}
```

#### 3. Create Posts Table (One-to-Many with Users)
```json
POST /table/with-relationships
{
  "schemaName": "posts",
  "projectUuid": "project-123",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) NOT NULL",
    "title": "VARCHAR(255) NOT NULL",
    "content": "TEXT",
    "status": "ENUM('draft', 'published', 'archived') DEFAULT 'draft'",
    "view_count": "INT DEFAULT 0",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
    "updated_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users",
      "referencedColumn": "id",
      "relationshipType": "one-to-many",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ]
}
```

#### 4. Create Categories Table
```json
POST /table
{
  "projectUuid": "project-123",
  "schemaName": "categories",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "name": "VARCHAR(100) UNIQUE NOT NULL",
    "description": "TEXT",
    "slug": "VARCHAR(100) UNIQUE NOT NULL",
    "is_active": "BOOLEAN DEFAULT TRUE",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  }
}
```

#### 5. Create Post-Categories Junction Table (Many-to-Many)
```json
POST /table/with-relationships
{
  "schemaName": "post_categories",
  "projectUuid": "project-123",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "post_id": "VARCHAR(36) NOT NULL",
    "category_id": "VARCHAR(36) NOT NULL",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  },
  "relationships": [
    {
      "foreignKeyColumn": "post_id",
      "referencedTable": "posts",
      "referencedColumn": "id",
      "relationshipType": "many-to-many",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    },
    {
      "foreignKeyColumn": "category_id",
      "referencedTable": "categories",
      "referencedColumn": "id",
      "relationshipType": "many-to-many",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ]
}
```

### Data Insertion Examples

#### Insert User
```json
POST /table/users/project/project-123/data
{
  "id": "user-001",
  "username": "johndoe",
  "email": "john@example.com",
  "password_hash": "hashed_password_here",
  "first_name": "John",
  "last_name": "Doe",
  "is_active": true
}
```

#### Insert User Profile
```json
POST /table/user_profiles/project/project-123/data
{
  "id": "profile-001",
  "user_id": "user-001",
  "bio": "Software developer passionate about technology",
  "phone": "+1-555-0123",
  "address": "123 Main St, City, State",
  "birth_date": "1990-05-15"
}
```

#### Insert Post
```json
POST /table/posts/project/project-123/data
{
  "id": "post-001",
  "user_id": "user-001",
  "title": "Getting Started with Spring Boot",
  "content": "Spring Boot makes it easy to create stand-alone applications...",
  "status": "published",
  "view_count": 0
}
```

#### Insert Category
```json
POST /table/categories/project/project-123/data
{
  "id": "cat-001",
  "name": "Technology",
  "description": "Posts about technology and programming",
  "slug": "technology",
  "is_active": true
}
```

#### Link Post to Category
```json
POST /table/post_categories/project/project-123/data
{
  "id": "pc-001",
  "post_id": "post-001",
  "category_id": "cat-001"
}
```

## Response Format

When retrieving data, relationships are returned as an array of objects:

```json
{
  "id": "schema-id",
  "schemaName": "posts",
  "projectId": "project-123",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) NOT NULL",
    "title": "VARCHAR(255) NOT NULL"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users",
      "referencedColumn": "id",
      "relationshipType": "one-to-many",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ],
  "createdAt": "2025-09-03T10:00:00Z",
  "updatedAt": "2025-09-03T10:00:00Z"
}
```

## Best Practices

1. **Primary Keys**: Always include a primary key field (usually `id`)
2. **Foreign Keys**: Use consistent naming (e.g., `user_id` for references to `users.id`)
3. **Timestamps**: Include `created_at` and `updated_at` fields for audit trails
4. **Constraints**: Use appropriate constraints (`NOT NULL`, `UNIQUE`, etc.)
5. **Default Values**: Set sensible defaults where applicable
6. **Relationship Types**: Use one-to-many as default for most cases
7. **Cascade Rules**: Consider the business logic when setting `onDelete` and `onUpdate`

## Error Handling

Common error responses:

```json
{
  "error": "Failed to create table: Table already exists",
  "schemaName": "users",
  "projectUuid": "project-123"
}
```

```json
{
  "error": "Failed to insert data: Foreign key constraint violation",
  "schemaName": "orders",
  "projectUuid": "project-123"
}
```

## Supported Data Types

- `VARCHAR(n)` - Variable length string
- `INT` - Integer
- `BIGINT` - Large integer
- `DECIMAL(p,s)` - Decimal number
- `BOOLEAN` - True/false
- `DATE` - Date only
- `TIMESTAMP` - Date and time
- `TEXT` - Large text field
- `ENUM('val1', 'val2')` - Enumerated values

## Security Notes

- All endpoints require valid JWT authentication
- Users can only access their own data unless explicitly granted permission
- Foreign key constraints help maintain data integrity
- Cascade operations should be used carefully to prevent unintended data loss
