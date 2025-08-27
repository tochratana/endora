# Enhanced Table Relationships Guide

## Overview

Your Spring application now has a comprehensive relationship management system that allows you to create and manage foreign key relationships between dynamic tables. This system provides full CRUD operations for relationships and advanced querying capabilities.

## 🚀 Key Features

### 1. **Create Tables with Relationships**
- Define foreign key relationships during table creation
- Support for CASCADE, SET NULL, and RESTRICT operations
- Automatic validation of referenced tables
- Project-scoped relationship management

### 2. **Dynamic Relationship Management**
- Add relationships to existing tables
- Remove relationships from tables
- View all relationships for a table
- Validate referential integrity

### 3. **Advanced Querying**
- Fetch records with joined data
- Get specific records with all related data
- Find tables that reference a specific table
- Bulk operations with relationship awareness

## 📋 API Endpoints

### Core Table Creation

#### 1. Create Table with Relationships
```http
POST /table/with-relationships
Content-Type: application/json

{
  "userUuid": "user-123",
  "projectUuid": "project-456",
  "schemaName": "orders",
  "schema": {
    "id": "String",
    "customer_id": "String",
    "product_id": "String",
    "quantity": "Integer",
    "total_amount": "Double"
  },
  "relationships": [
    {
      "foreignKeyColumn": "customer_id",
      "referencedTable": "customers",
      "referencedColumn": "id",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    },
    {
      "foreignKeyColumn": "product_id",
      "referencedTable": "products",
      "referencedColumn": "id",
      "onDelete": "RESTRICT",
      "onUpdate": "CASCADE"
    }
  ]
}
```

### Relationship Management

#### 2. Get Table Relationships
```http
GET /table/{schemaName}/project/{projectUuid}/relationships
```

**Response:**
```json
{
  "schemaName": "orders",
  "projectUuid": "project-456",
  "relationships": [
    {
      "foreignKeyColumn": "customer_id",
      "referencedTable": "customers",
      "referencedColumn": "id",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ],
  "count": 1
}
```

#### 3. Add Relationship to Existing Table
```http
POST /table/{schemaName}/project/{projectUuid}/relationships
Content-Type: application/json

{
  "foreignKeyColumn": "category_id",
  "referencedTable": "categories",
  "referencedColumn": "id",
  "onDelete": "SET NULL",
  "onUpdate": "CASCADE"
}
```

#### 4. Remove Relationship
```http
DELETE /table/{schemaName}/project/{projectUuid}/relationships/{foreignKeyColumn}
```

### Advanced Querying

#### 5. Get Record with All Relations
```http
GET /table/{schemaName}/project/{projectUuid}/record/{id}/with-relations
```

**Response Example:**
```json
{
  "schemaName": "orders",
  "projectUuid": "project-456",
  "recordId": "order-123",
  "record": {
    "id": "order-123",
    "customer_id": "cust-001",
    "product_id": "prod-001",
    "quantity": 2,
    "total_amount": 199.99,
    "createdAt": "2025-08-26T10:00:00Z",
    "customer_id_data": {
      "id": "cust-001",
      "name": "John Doe",
      "email": "john@example.com"
    },
    "product_id_data": {
      "id": "prod-001",
      "name": "Laptop",
      "price": 99.99
    }
  }
}
```

#### 6. Get Records with Joins
```http
POST /table/{schemaName}/project/{projectUuid}/records-with-joins
Content-Type: application/json

["customers", "products"]
```

#### 7. Get Related Tables
```http
GET /table/{schemaName}/project/{projectUuid}/related-tables
```

#### 8. Validate Relationship Integrity
```http
GET /table/{schemaName}/project/{projectUuid}/validate-integrity
```

## 🔧 Implementation Details

### Relationship Types Supported

1. **One-to-Many**: Most common relationship type
   - Example: One customer can have many orders
   - Implemented via foreign key in the "many" table

2. **Foreign Key Constraints**:
   - `CASCADE`: Delete/update related records automatically
   - `SET NULL`: Set foreign key to null when referenced record is deleted
   - `RESTRICT`: Prevent deletion if related records exist

### Data Storage

- **Table Schemas**: Stored in MongoDB with relationship metadata
- **Table Data**: Stored in MongoDB with foreign key values
- **Relationships**: Embedded in table schema documents
- **Validation**: Real-time validation of referential integrity

### Key Features

1. **Project Scoping**: All relationships are scoped to projects
2. **User Validation**: Ensures users can only manage their own tables
3. **Automatic Validation**: Validates referenced tables exist before creating relationships
4. **Referential Integrity**: Checks and maintains data consistency
5. **Dynamic Endpoints**: Automatically generates REST endpoints for new tables

## 📝 Usage Examples

### Example 1: E-commerce System

```bash
# 1. Create customers table
curl -X POST http://localhost:8080/table \
  -H "Content-Type: application/json" \
  -d '{
    "userUuid": "user-123",
    "projectUuid": "ecommerce-project",
    "schemaName": "customers",
    "schema": {
      "name": "String",
      "email": "String",
      "phone": "String"
    }
  }'

# 2. Create products table
curl -X POST http://localhost:8080/table \
  -H "Content-Type: application/json" \
  -d '{
    "userUuid": "user-123",
    "projectUuid": "ecommerce-project",
    "schemaName": "products",
    "schema": {
      "name": "String",
      "price": "Double",
      "description": "String"
    }
  }'

# 3. Create orders table with relationships
curl -X POST http://localhost:8080/table/with-relationships \
  -H "Content-Type: application/json" \
  -d '{
    "userUuid": "user-123",
    "projectUuid": "ecommerce-project",
    "schemaName": "orders",
    "schema": {
      "customer_id": "String",
      "product_id": "String",
      "quantity": "Integer",
      "order_date": "String"
    },
    "relationships": [
      {
        "foreignKeyColumn": "customer_id",
        "referencedTable": "customers",
        "referencedColumn": "id",
        "onDelete": "CASCADE",
        "onUpdate": "CASCADE"
      },
      {
        "foreignKeyColumn": "product_id",
        "referencedTable": "products",
        "referencedColumn": "id",
        "onDelete": "RESTRICT",
        "onUpdate": "CASCADE"
      }
    ]
  }'
```

### Example 2: Adding Relationships Later

```bash
# Add a category relationship to existing products table
curl -X POST http://localhost:8080/table/products/project/ecommerce-project/relationships \
  -H "Content-Type: application/json" \
  -d '{
    "foreignKeyColumn": "category_id",
    "referencedTable": "categories",
    "referencedColumn": "id",
    "onDelete": "SET NULL",
    "onUpdate": "CASCADE"
  }'
```

### Example 3: Querying Related Data

```bash
# Get an order with all related customer and product data
curl -X GET http://localhost:8080/table/orders/project/ecommerce-project/record/order-123/with-relations

# Get all orders with joined customer and product data
curl -X POST http://localhost:8080/table/orders/project/ecommerce-project/records-with-joins \
  -H "Content-Type: application/json" \
  -d '["customers", "products"]'
```

## 🛡️ Security & Validation

### User Authentication
- All endpoints require valid user UUID
- Users can only access tables in their own projects
- Project ownership validation on all operations

### Data Validation
- Referenced tables must exist in the same project
- Foreign key columns must exist in table schema
- Referential integrity checks before data operations
- Prevents circular relationships

### Error Handling
- Comprehensive error messages
- Graceful handling of missing references
- Validation errors with specific details
- Transaction-like behavior for relationship operations

## 🔄 Integration with Existing Features

### Dynamic Endpoints
- New tables with relationships automatically get REST endpoints
- Relationship data included in dynamic endpoint responses
- Full CRUD operations maintain referential integrity

### MongoDB Integration
- Efficient storage of relationship metadata
- Fast lookups for related data
- Optimized queries for joined data retrieval

### Swagger Documentation
- All relationship endpoints documented in Swagger UI
- Interactive API testing with relationship features
- Complete API specification for integration

## 📊 Performance Considerations

### Optimizations
- Lazy loading of related data when requested
- Efficient MongoDB queries for relationship lookups
- Caching of table schema metadata
- Batch operations for multiple relationships

### Best Practices
- Use appropriate relationship constraints (CASCADE vs RESTRICT)
- Index foreign key columns for better performance
- Validate data integrity regularly
- Monitor relationship complexity to avoid deep nesting

## 🎯 Next Steps

Your enhanced relationship system is now ready! You can:

1. **Start Creating Related Tables**: Use the `/table/with-relationships` endpoint
2. **Test Relationship Queries**: Try the various join and relation endpoints
3. **Validate Data Integrity**: Use the integrity validation endpoint
4. **Build Complex Data Models**: Create multi-table relationships for your use cases

The system provides a solid foundation for building complex, related data structures while maintaining the flexibility of your dynamic table creation system.
