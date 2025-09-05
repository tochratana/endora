# Mini Supabase-like Platform

This project is a simplified Supabase-style backend built with **Spring Boot 3.3** and **Java 21**. Each project can have its own PostgreSQL schema with users, roles and policies. Authentication uses JWT (HS256) and all non-auth endpoints require a valid token.

## Running locally

1. **Start Postgres**

```bash
docker compose up -d
```

2. **Set environment variables** (defaults are shown):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=dev-secret
```

3. **Run the application**

```bash
mvn spring-boot:run
```

## Sample workflow

```bash
# Create project
curl -X POST localhost:8080/api/projects -H 'Content-Type: application/json' -d '{"name":"demo","authEnabled":true}'

# Register and login
curl -X POST localhost:8080/api/<projectId>/auth/register -H 'Content-Type: application/json' -d '{"username":"u1","email":"u1@e.com","password":"pass"}'

TOKEN=$(curl -s -X POST localhost:8080/api/<projectId>/auth/login -H 'Content-Type: application/json' -d '{"usernameOrEmail":"u1","password":"pass"}' | jq -r .accessToken)

# Create posts table and policy
curl -X POST localhost:8080/api/<projectId>/tables -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"name":"posts","columns":[{"name":"title","type":"text"},{"name":"content","type":"text"}]}'

curl -X POST localhost:8080/api/<projectId>/policies -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"table":"posts","action":"select","role":"authenticated"}'

# Fetch posts (empty array)
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/<projectId>/posts
```

## Acceptance tests

1. Creating a project with `authEnabled=false` does not create a schema; all non-auth endpoints still require JWT.
2. Creating a project with `authEnabled=true` provisions per-project schema with base tables.
3. Register/login returns a JWT containing `project_id` and `roles=["authenticated"]`.
4. Accessing `/api/{projectId}/posts` without policies returns `403`.
5. After adding a policy allowing `select` for role `authenticated`, `/posts` succeeds.
