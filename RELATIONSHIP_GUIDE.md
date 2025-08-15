# Complete Guide: Getting Data from Tables with Relationships

## Step-by-Step Example

### Step 1: Create Tables with Relationships

First, create a basic table (users):
```bash
POST /table/with-relationships
Content-Type: application/json

{
  "tableName": "users",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid",
  "schema": {
    "name": "VARCHAR(255)",
    "email": "VARCHAR(255)",
    "age": "INTEGER"
  },
  "relationships": []
}
```

Then create a table that references it (orders):
```bash
POST /table/with-relationships
Content-Type: application/json

{
  "tableName": "orders",
  "projectUuid": "your-project-uuid", 
  "userUuid": "your-user-uuid",
  "schema": {
    "order_date": "TIMESTAMP",
    "total_amount": "DECIMAL(10,2)",
    "user_id": "VARCHAR(255)",
    "status": "VARCHAR(50)"
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

### Step 2: Insert Sample Data

Insert a user:
```bash
POST /table/data
Content-Type: application/json

{
  "tableName": "users",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid",
  "data": {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "age": 30
  }
}
```

Response will include the MongoDB ObjectId:
```json
{
  "message": "Data inserted successfully",
  "tableName": "users",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid"
}
```

Get the user's MongoDB ObjectId:
```bash
GET /table/users/data/project/your-project-uuid
```

Response:
```json
[
  {
    "id": "64f7b1c8e4b0a1b2c3d4e5f9",
    "name": "John Doe",
    "email": "john.doe@example.com", 
    "age": 30,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

Insert an order using the user's ObjectId:
```bash
POST /table/data
Content-Type: application/json

{
  "tableName": "orders",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid", 
  "data": {
    "order_date": "2024-01-15T10:30:00Z",
    "total_amount": 99.99,
    "user_id": "64f7b1c8e4b0a1b2c3d4e5f9",
    "status": "pending"
  }
}
```

### Step 3: Get Data with Relationships

#### Option 1: Get Single Record with Relations
```bash
GET /table/orders/64f7b1c8e4b0a1b2c3d4e5f8/with-relations/project/your-project-uuid
```

Response (with user data automatically loaded):
```json
{
  "id": "64f7b1c8e4b0a1b2c3d4e5f8",
  "order_date": "2024-01-15T10:30:00Z",
  "total_amount": 99.99,
  "user_id": "64f7b1c8e4b0a1b2c3d4e5f9",
  "status": "pending",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "user_id_data": {
    "id": "64f7b1c8e4b0a1b2c3d4e5f9",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "age": 30,
    "createdAt": "2024-01-10T09:00:00",
    "updatedAt": "2024-01-10T09:00:00"
  }
}
```

#### Option 2: Find Which Tables Reference a Table
```bash
GET /table/users/relationships/project/your-project-uuid
```

Response (shows all tables that reference users):
```json
[
  {
    "id": "64f7b1c8e4b0a1b2c3d4e5f6",
    "tableName": "orders",
    "projectId": "your-project-uuid",
    "schema": {
      "order_date": "TIMESTAMP",
      "total_amount": "DECIMAL(10,2)",
      "user_id": "VARCHAR(255)",
      "status": "VARCHAR(50)"
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
]
```

#### Option 3: Get All Records from Table (without relations)
```bash
GET /table/orders/data/project/your-project-uuid
```

Response (basic data without relations):
```json
[
  {
    "id": "64f7b1c8e4b0a1b2c3d4e5f8",
    "order_date": "2024-01-15T10:30:00Z",
    "total_amount": 99.99,
    "user_id": "64f7b1c8e4b0a1b2c3d4e5f9",
    "status": "pending",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

## Advanced Examples

### Many-to-Many Relationship (Joint Table)

Create a roles table:
```bash
POST /table/with-relationships
{
  "tableName": "roles",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid",
  "schema": {
    "role_name": "VARCHAR(255)",
    "description": "TEXT"
  },
  "relationships": []
}
```

Create a user_roles joint table:
```bash
POST /table/with-relationships
{
  "tableName": "user_roles",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid", 
  "schema": {
    "user_id": "VARCHAR(255)",
    "role_id": "VARCHAR(255)",
    "assigned_date": "TIMESTAMP"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users",
      "referencedColumn": "id"
    },
    {
      "foreignKeyColumn": "role_id", 
      "referencedTable": "roles",
      "referencedColumn": "id"
    }
  ]
}
```

Get user_roles record with both user and role data:
```bash
GET /table/user_roles/64f7b1c8e4b0a1b2c3d4e5fa/with-relations/project/your-project-uuid
```

Response:
```json
{
  "id": "64f7b1c8e4b0a1b2c3d4e5fa",
  "user_id": "64f7b1c8e4b0a1b2c3d4e5f9",
  "role_id": "64f7b1c8e4b0a1b2c3d4e5fb", 
  "assigned_date": "2024-01-15T10:30:00Z",
  "user_id_data": {
    "id": "64f7b1c8e4b0a1b2c3d4e5f9",
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "role_id_data": {
    "id": "64f7b1c8e4b0a1b2c3d4e5fb",
    "role_name": "Admin",
    "description": "Administrator role"
  }
}
```

### Custom Column References

Instead of referencing MongoDB ObjectId, you can reference custom columns:

```bash
POST /table/with-relationships
{
  "tableName": "orders",
  "projectUuid": "your-project-uuid",
  "userUuid": "your-user-uuid",
  "schema": {
    "order_date": "TIMESTAMP", 
    "user_email": "VARCHAR(255)",
    "total_amount": "DECIMAL(10,2)"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_email",
      "referencedTable": "users", 
      "referencedColumn": "email"
    }
  ]
}
```

This will match the `user_email` field in orders with the `email` field in users.

## Debugging Relationships

If relationships aren't working, use the debug endpoint:
```bash
GET /table/debug/project/your-project-uuid
```

Check your server console for detailed debug information about relationships.

## Key Points

1. **MongoDB ObjectIds**: Use the `id` field returned from data insertion
2. **Foreign Key Values**: Must match exactly (case-sensitive)
3. **Project Scope**: Relationships only work within the same project
4. **Automatic Loading**: Related data is automatically loaded and added with `_data` suffix
5. **Null Handling**: If related record doesn't exist, `_data` field will be `null`

## Error Troubleshooting

- **Empty relationships**: Check debug endpoint to see if relationships are saved
- **Null `_data` fields**: Verify foreign key values match existing records
- **404 responses**: Ensure table and project exist and belong to the user
