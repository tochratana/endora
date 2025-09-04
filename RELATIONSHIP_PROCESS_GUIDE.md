# Complete Relationship Process Guide

## Overview
This guide walks you through the entire process of creating, managing, and working with relationships in your SpringTwoDataSource system.

## Step-by-Step Process

### 1. Understanding Relationship Types

Your system supports three types of relationships, all returned as **arrays of objects**:

- **One-to-One** (e.g., User ↔ Profile) - Returns array with 0 or 1 object
- **One-to-Many** (e.g., User → Orders) - Returns array with 0 or more objects (**DEFAULT**)
- **Many-to-Many** (e.g., Posts ↔ Categories) - Returns array with 0 or more objects

### 2. Creating Tables with Relationships

#### Step 2.1: Create Parent Table First
Always create the referenced (parent) table before creating tables that reference it.

```bash
# Create Users table (parent)
POST /table/with-relationships
```

```json
{
  "schemaName": "users",
  "projectUuid": "your-project-uuid",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "name": "VARCHAR(255) NOT NULL",
    "email": "VARCHAR(255) UNIQUE NOT NULL",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  },
  "relationships": []
}
```

#### Step 2.2: Create Child Table with Relationship
Create tables that reference the parent table.

```bash
# Create Orders table (child)
POST /table/with-relationships
```

```json
{
  "schemaName": "orders",
  "projectUuid": "your-project-uuid",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) NOT NULL",
    "product_name": "VARCHAR(255) NOT NULL",
    "total_amount": "DECIMAL(10,2)",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
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

### 3. Inserting Data with Relationships

#### Step 3.1: Insert Parent Record First
```bash
# Insert User
POST /table/users/project/your-project-uuid/data
```

```json
{
  "id": "user-001",
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### Step 3.2: Insert Child Records
```bash
# Insert Order (references user-001)
POST /table/orders/project/your-project-uuid/data
```

```json
{
  "id": "order-001",
  "user_id": "user-001",
  "product_name": "Laptop",
  "total_amount": 999.99
}
```

```bash
# Insert another Order
POST /table/orders/project/your-project-uuid/data
```

```json
{
  "id": "order-002",
  "user_id": "user-001",
  "product_name": "Mouse",
  "total_amount": 29.99
}
```

### 4. Retrieving Data with Relationships

#### Option 1: Get All Records with Relationships
```bash
GET /api/tables/users/project/your-project-uuid
```

**Response:**
```json
[
  {
    "id": "user-001",
    "name": "John Doe",
    "email": "john@example.com",
    "createdAt": "2025-09-03T10:00:00Z",
    "updatedAt": "2025-09-03T10:00:00Z",
    "orders": [
      {
        "id": "order-001",
        "user_id": "user-001",
        "product_name": "Laptop",
        "total_amount": 999.99,
        "createdAt": "2025-09-03T11:00:00Z"
      },
      {
        "id": "order-002",
        "user_id": "user-001",
        "product_name": "Mouse",
        "total_amount": 29.99,
        "createdAt": "2025-09-03T12:00:00Z"
      }
    ]
  }
]
```

#### Option 2: Get Single Record with Relationships
```bash
GET /api/tables/users/user-001/project/your-project-uuid/relations
```

**Response:**
```json
{
  "id": "user-001",
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-09-03T10:00:00Z",
  "updatedAt": "2025-09-03T10:00:00Z",
  "orders": [
    {
      "id": "order-001",
      "user_id": "user-001",
      "product_name": "Laptop",
      "total_amount": 999.99,
      "createdAt": "2025-09-03T11:00:00Z"
    },
    {
      "id": "order-002",
      "user_id": "user-001",
      "product_name": "Mouse",
      "total_amount": 29.99,
      "createdAt": "2025-09-03T12:00:00Z"
    }
  ]
}
```

### 5. Common Relationship Patterns

#### Pattern 1: One-to-One (User → Profile)

**Create Profile Table:**
```json
{
  "schemaName": "user_profiles",
  "projectUuid": "your-project-uuid",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) UNIQUE NOT NULL",
    "bio": "TEXT",
    "avatar_url": "VARCHAR(500)",
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

#### Pattern 2: Many-to-Many (Posts ↔ Categories)

**Step 1: Create Categories Table**
```json
{
  "schemaName": "categories",
  "projectUuid": "your-project-uuid",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "name": "VARCHAR(100) UNIQUE NOT NULL",
    "slug": "VARCHAR(100) UNIQUE NOT NULL",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  },
  "relationships": []
}
```

**Step 2: Create Posts Table**
```json
{
  "schemaName": "posts",
  "projectUuid": "your-project-uuid",
  "schema": {
    "id": "VARCHAR(36) PRIMARY KEY",
    "user_id": "VARCHAR(36) NOT NULL",
    "title": "VARCHAR(255) NOT NULL",
    "content": "TEXT",
    "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users",
      "referencedColumn": "id",
      "relationshipType": "one-to-many"
    }
  ]
}
```

**Step 3: Create Junction Table**
```json
{
  "schemaName": "post_categories",
  "projectUuid": "your-project-uuid",
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
      "relationshipType": "many-to-many"
    },
    {
      "foreignKeyColumn": "category_id",
      "referencedTable": "categories",
      "referencedColumn": "id",
      "relationshipType": "many-to-many"
    }
  ]
}
```

### 6. Advanced Relationship Management

#### Check Table Relationships
```bash
GET /table/users/project/your-project-uuid
```

This returns the table schema with all defined relationships.

#### Add Relationship to Existing Table
You can add relationships to existing tables by updating the schema.

### 7. Best Practices for Relationships

#### 7.1: Naming Conventions
- Foreign key columns: `{table_name}_id` (e.g., `user_id`, `category_id`)
- Junction tables: `{table1}_{table2}` (e.g., `post_categories`, `user_roles`)

#### 7.2: Relationship Order
1. Create parent tables first
2. Create child tables with relationships
3. Create junction tables for many-to-many relationships

#### 7.3: Data Insertion Order
1. Insert parent records first
2. Insert child records with valid foreign keys
3. Insert junction table records last

#### 7.4: Cascade Rules
- **CASCADE**: Delete/update related records automatically
- **SET NULL**: Set foreign key to NULL when parent is deleted
- **RESTRICT**: Prevent deletion if related records exist

### 8. Troubleshooting Common Issues

#### Issue 1: "Referenced table does not exist"
**Solution:** Create the referenced table first before creating the relationship.

#### Issue 2: "Foreign key constraint violation"
**Solution:** Ensure the referenced record exists before inserting child records.

#### Issue 3: "Table already exists"
**Solution:** Check if the table was already created or use a different schema name.

#### Issue 4: Relationships not showing in response
**Solution:** Use the enhanced endpoints:
- `/api/tables/{schemaName}/project/{projectUuid}` for all records
- `/api/tables/{schemaName}/{id}/project/{projectUuid}/relations` for single record

### 9. Complete Example Workflow

Here's a complete example creating a blog system:

#### Step 1: Create Users
```bash
curl -X POST http://localhost:8080/table/with-relationships \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "users",
    "projectUuid": "blog-project-123",
    "schema": {
      "id": "VARCHAR(36) PRIMARY KEY",
      "username": "VARCHAR(50) UNIQUE NOT NULL",
      "email": "VARCHAR(255) UNIQUE NOT NULL",
      "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    },
    "relationships": []
  }'
```

#### Step 2: Create Posts
```bash
curl -X POST http://localhost:8080/table/with-relationships \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "schemaName": "posts",
    "projectUuid": "blog-project-123",
    "schema": {
      "id": "VARCHAR(36) PRIMARY KEY",
      "user_id": "VARCHAR(36) NOT NULL",
      "title": "VARCHAR(255) NOT NULL",
      "content": "TEXT",
      "created_at": "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    },
    "relationships": [
      {
        "foreignKeyColumn": "user_id",
        "referencedTable": "users",
        "referencedColumn": "id",
        "relationshipType": "one-to-many"
      }
    ]
  }'
```

#### Step 3: Insert Data
```bash
# Insert User
curl -X POST http://localhost:8080/table/users/project/blog-project-123/data \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "user-001",
    "username": "johndoe",
    "email": "john@example.com"
  }'

# Insert Post
curl -X POST http://localhost:8080/table/posts/project/blog-project-123/data \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "post-001",
    "user_id": "user-001",
    "title": "My First Post",
    "content": "This is the content of my first post."
  }'
```

#### Step 4: Retrieve with Relationships
```bash
# Get all users with their posts
curl -X GET http://localhost:8080/api/tables/users/project/blog-project-123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get specific user with posts
curl -X GET http://localhost:8080/api/tables/users/user-001/project/blog-project-123/relations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 10. Key Endpoints Summary

| Operation | Endpoint | Description |
|-----------|----------|-------------|
| Create Table | `POST /table` | Basic table creation |
| Create with Relations | `POST /table/with-relationships` | Table with relationships |
| Insert Data | `POST /table/{schema}/project/{uuid}/data` | Insert record |
| Get All (No Relations) | `GET /api/tables/{schema}` | Basic records |
| Get All (With Relations) | `GET /api/tables/{schema}/project/{uuid}` | Records with relationships |
| Get One (With Relations) | `GET /api/tables/{schema}/{id}/project/{uuid}/relations` | Single record with relationships |

### 11. Relationship Response Format

All relationships are returned as arrays of objects, providing a consistent structure for your frontend applications:

```json
{
  "parentRecord": "data",
  "relationshipName": [
    {
      "id": "related-record-1",
      "data": "values"
    },
    {
      "id": "related-record-2", 
      "data": "values"
    }
  ]
}
```

This guide covers the complete relationship process from creation to retrieval. Follow these steps in order for the best results!
