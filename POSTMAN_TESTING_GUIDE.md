# 🚀 Postman Collection for Table Relationships Testing

## 📋 How to Import and Use

### Step 1: Import Collection
1. Open Postman
2. Click "Import" button
3. Select the file: `Table_Relationships_Postman_Collection.json`
4. Click "Import"

### Step 2: Set Environment Variables
The collection uses these variables (already configured):
- `baseUrl`: `http://localhost:8080` (change if your server runs on different port)
- `customerId`: Sample MongoDB ObjectId (update with real IDs from your data)
- `productId`: Sample MongoDB ObjectId (update with real IDs from your data)  
- `orderId`: Sample MongoDB ObjectId (update with real IDs from your data)

## 📁 Collection Structure

### 1. **Setup - Create Base Tables** (3 requests)
- Create Customers Table
- Create Products Table
- Create Categories Table

### 2. **Create Tables with Relationships** (2 requests)
- Create Orders Table with Multiple Relationships
- Create Order Items Table with Relationships

### 3. **Add Sample Data** (3 requests)
- Add Customer Data
- Add Product Data
- Add Category Data

### 4. **Relationship Management** (5 requests)
- Get Table Relationships
- Add Relationship to Existing Table
- Remove Relationship from Table
- Get Related Tables
- Validate Relationship Integrity

### 5. **Advanced Querying with Relationships** (3 requests)
- Get Record with All Relations
- Get Records with Joins
- Get Order Items with Joins

### 6. **Table Information** (3 requests)
- Get All Tables in Project
- Get Specific Table Schema
- Get Table Data

### 7. **Error Testing** (3 requests)
- Try Creating Table with Invalid Relationship
- Try Adding Duplicate Relationship
- Try Removing Non-existent Relationship

### 8. **Complete Workflow Example** (4 requests)
- Step-by-step workflow showing relationship features

## 🎯 Recommended Testing Flow

### Phase 1: Basic Setup
1. Run all requests in "Setup - Create Base Tables"
2. Run all requests in "Add Sample Data"
3. **Important**: Copy the returned MongoDB ObjectIDs from the responses and update the Postman variables

### Phase 2: Create Related Tables
1. Run "Create Orders Table with Multiple Relationships"
2. Run "Create Order Items Table with Relationships"

### Phase 3: Test Relationship Features
1. Test "Get Table Relationships" to see defined relationships
2. Test "Add Relationship to Existing Table" to add category relationship to products
3. Test "Get Related Tables" to find tables referencing customers

### Phase 4: Advanced Querying
1. Add an order using "Step 1: Add Order with Foreign Keys"
2. Test "Get Record with All Relations" to see joined data
3. Test "Get Records with Joins" for bulk queries

### Phase 5: Validation & Error Handling
1. Run "Validate Relationship Integrity"
2. Test error scenarios in "Error Testing" section

## 🔧 Key Testing Points

### Test Data Relationships
Each request includes realistic e-commerce data:
```json
{
  "customers": {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123"
  },
  "products": {
    "name": "Laptop Computer",
    "price": 1299.99,
    "category": "Electronics"
  },
  "orders": {
    "customer_id": "{{customerId}}",
    "product_id": "{{productId}}",
    "quantity": 2,
    "total_amount": 2599.98
  }
}
```

### Relationship Types Tested
- **One-to-Many**: Customer → Orders
- **Many-to-One**: Orders → Customer, Orders → Product
- **Constraint Types**: CASCADE, RESTRICT, SET NULL

### Expected Response Examples

**Get Record with Relations Response:**
```json
{
  "record": {
    "id": "order-123",
    "customer_id": "cust-001",
    "product_id": "prod-001",
    "quantity": 2,
    "customer_id_data": {
      "id": "cust-001",
      "name": "John Doe",
      "email": "john.doe@example.com"
    },
    "product_id_data": {
      "id": "prod-001",
      "name": "Laptop Computer",
      "price": 1299.99
    }
  }
}
```

## ⚠️ Important Notes

### Before Testing:
1. **Start your Spring Boot application** on port 8080
2. **Ensure MongoDB is running** and connected
3. **Update variables** with real MongoDB ObjectIDs after creating data

### During Testing:
1. **Follow the sequence** - create base tables before related tables
2. **Copy IDs from responses** and update Postman variables
3. **Check response status codes** - 200 for success, 400 for errors
4. **Validate JSON responses** for expected data structure

### Common Issues:
- **404 errors**: Table or record doesn't exist - check table names and IDs
- **400 errors**: Invalid relationships - ensure referenced tables exist
- **Foreign key validation**: Make sure foreign key values reference existing records

## 🎉 What You'll Test

✅ **Create tables with foreign key relationships**
✅ **Add/remove relationships dynamically**  
✅ **Query records with automatic joins**
✅ **Validate referential integrity**
✅ **Error handling for invalid operations**
✅ **Complete e-commerce workflow with relationships**
✅ **Bulk operations with relationship awareness**

The collection covers all the enhanced relationship features I implemented in your Spring application!
