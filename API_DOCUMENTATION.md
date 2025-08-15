# Updated API Documentation with UUID Support

## Base URL
All endpoints use base URL: `http://localhost:8080`

## User Management APIs

### 1. Create User
- **Method**: `POST`
- **Endpoint**: `/user`
- **Description**: Creates a new user with auto-generated UUID
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```
- **Response**:
```json
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

### 2. Get All Users
- **Method**: `GET`
- **Endpoint**: `/user`
- **Request Body**: None

---

## Project Management APIs

### 1. Create Project (UPDATED - Now requires userUuid)
- **Method**: `POST`
- **Endpoint**: `/projects`
- **Description**: Creates a new project with auto-generated projectUuid
- **Request Body**:
```json
{
  "userUuid": "550e8400-e29b-41d4-a716-446655440000",
  "projectName": "E-commerce API",
  "description": "Backend API for e-commerce platform"
}
```
- **Response**:
```json
{
  "message": "Project created successfully",
  "project": {
    "id": "64f7b1c8e4b0a1b2c3d4e5f6",
    "userUuid": "550e8400-e29b-41d4-a716-446655440000",
    "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
    "projectName": "E-commerce API",
    "description": "Backend API for e-commerce platform"
  },
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Get All Projects
- **Method**: `GET`
- **Endpoint**: `/projects`
- **Request Body**: None

### 3. Get Project by ID
- **Method**: `GET`
- **Endpoint**: `/projects/{id}`
- **Request Body**: None

### 4. Get Project by UUID (NEW)
- **Method**: `GET`
- **Endpoint**: `/projects/uuid/{projectUuid}`
- **Request Body**: None

### 5. Get Projects by User UUID (NEW)
- **Method**: `GET`
- **Endpoint**: `/projects/user/{userUuid}`
- **Request Body**: None

### 6. Get Project with User Details
- **Method**: `GET`
- **Endpoint**: `/projects/{id}/with-user`
- **Request Body**: None

### 7. Delete Project
- **Method**: `DELETE`
- **Endpoint**: `/projects/{id}`
- **Request Body**: None

---

## Table Management APIs

### 1. Create Table (UPDATED - Now requires userUuid and projectUuid)
- **Method**: `POST`
- **Endpoint**: `/table`
- **Description**: Creates a new table with user and project validation
- **Request Body**:
```json
{
  "tableName": "users_table",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000",
  "schema": {
    "name": "VARCHAR(255)",
    "email": "VARCHAR(255)",
    "age": "INTEGER",
    "created_at": "TIMESTAMP"
  }
}
```
- **Response**:
```json
{
  "message": "Table created successfully",
  "tableName": "users_table",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Create Table with Relationships (UPDATED)
- **Method**: `POST`
- **Endpoint**: `/table/with-relationships`
- **Description**: Creates a table with foreign key relationships
- **Request Body**:
```json
{
  "tableName": "orders",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000",
  "schema": {
    "order_date": "TIMESTAMP",
    "total_amount": "DECIMAL(10,2)",
    "user_id": "VARCHAR(255)",
    "status": "VARCHAR(50)"
  },
  "relationships": [
    {
      "foreignKeyColumn": "user_id",
      "referencedTable": "users_table",
      "referencedColumn": "id",
      "onDelete": "CASCADE",
      "onUpdate": "CASCADE"
    }
  ]
}
```
- **Response**:
```json
{
  "message": "Table with relationships created successfully",
  "tableName": "orders",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000",
  "relationships": 1
}
```

### 3. Get All Tables
- **Method**: `GET`
- **Endpoint**: `/table`
- **Request Body**: None

### 4. Get Tables by Project UUID (UPDATED)
- **Method**: `GET`
- **Endpoint**: `/table/project/{projectUuid}`
- **Request Body**: None

### 5. Get Table by Name and Project UUID (UPDATED)
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/project/{projectUuid}`
- **Request Body**: None

---

## Data Management APIs

### 1. Insert Data (UPDATED - Now requires userUuid and projectUuid)
- **Method**: `POST`
- **Endpoint**: `/table/data`
- **Description**: Inserts data into a table with user validation
- **Request Body**:
```json
{
  "tableName": "users_table",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000",
  "data": {
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "age": 28,
    "created_at": "2024-01-15T10:30:00Z"
  }
}
```
- **Response**:
```json
{
  "message": "Data inserted successfully",
  "tableName": "users_table",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Insert Data (Alternative Path Method) (UPDATED)
- **Method**: `POST`
- **Endpoint**: `/table/{tableName}/data/project/{projectUuid}/user/{userUuid}`
- **Description**: Inserts data using path parameters for validation
- **Request Body**:
```json
{
  "name": "Bob Johnson",
  "email": "bob.johnson@example.com",
  "age": 35,
  "created_at": "2024-01-15T11:00:00Z"
}
```
- **Response**:
```json
{
  "message": "Data inserted successfully",
  "tableName": "users_table",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. Get All Table Data
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/data`
- **Request Body**: None

### 4. Get Table Data by Project UUID (UPDATED)
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/data/project/{projectUuid}`
- **Request Body**: None

### 5. Get Record by ID (UPDATED)
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/{id}/project/{projectUuid}`
- **Request Body**: None

### 6. Update Record by ID (UPDATED)
- **Method**: `PUT`
- **Endpoint**: `/table/{tableName}/{id}/project/{projectUuid}`
- **Description**: Updates specific fields of a record
- **Request Body**:
```json
{
  "name": "Jane Smith Updated",
  "age": 29
}
```
- **Response**:
```json
{
  "message": "Record updated successfully",
  "tableName": "users_table",
  "id": "64f7b1c8e4b0a1b2c3d4e5f7",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001"
}
```

### 7. Delete Record by ID (UPDATED)
- **Method**: `DELETE`
- **Endpoint**: `/table/{tableName}/{id}/project/{projectUuid}`
- **Request Body**: None
- **Response**:
```json
{
  "message": "Record deleted successfully",
  "tableName": "users_table",
  "id": "64f7b1c8e4b0a1b2c3d4e5f7",
  "projectUuid": "750e8400-e29b-41d4-a716-446655440001"
}
```

---

## Documentation APIs

### 1. Get Table Documentation
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/docs`
- **Request Body**: None

### 2. Get All Documentation
- **Method**: `GET`
- **Endpoint**: `/table/docs`
- **Request Body**: None

---

## Relationship APIs

### 1. Get Related Tables (UPDATED)
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/relationships/project/{projectUuid}`
- **Request Body**: None

### 2. Get Record with Relations (UPDATED)
- **Method**: `GET`
- **Endpoint**: `/table/{tableName}/{id}/with-relations/project/{projectUuid}`
- **Request Body**: None

---

## Important Notes

### UUID Requirements:
1. **User Creation**: UUIDs are automatically generated when creating users
2. **Project Creation**: Requires valid `userUuid`, auto-generates `projectUuid`
3. **Table Creation**: Requires both `userUuid` and `projectUuid` for validation
4. **Data Operations**: All data operations require user and project validation

### Validation Rules:
- Users must exist before creating projects
- Projects must belong to the specified user
- Tables can only be created by project owners
- Data can only be inserted/modified by project owners

### Error Responses:
All endpoints return standardized error responses:
```json
{
  "error": "Error type",
  "message": "Detailed error message"
}
```

### Success Responses:
Most modification endpoints return confirmation with relevant IDs:
```json
{
  "message": "Operation completed successfully",
  "tableName": "table_name",
  "projectUuid": "project-uuid",
  "userUuid": "user-uuid"
}
```
