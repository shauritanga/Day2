# Refactoring a Monolith into Microservices

In this lesson, you will learn how to refactor a Spring Boot monolith into microservices gradually.

We will not rewrite the whole system at once.

Instead, we will extract one business capability first:

```text
Hospitals
```

The rest of the system will continue running inside the monolith.

This is how many real systems are refactored. You move one part at a time, prove that it works, then continue with the next part.

## 1. What This Project Is About

This project is a simplified Claims Management Information System.

It has these business areas:

| Business area | API path | Current owner after first refactor |
| --- | --- | --- |
| Hospitals | `/api/hospitals/**` | `hospital-service` |
| Members | `/api/members/**` | `cmis-monolith` |
| Claims | `/api/claims/**` | `cmis-monolith` |
| Adjudications | `/api/adjudications/**` | `cmis-monolith` |

Only hospitals have been refactored into a separate service.

Members, claims, and adjudications are still inside the monolith.

That is intentional.

The purpose of this lesson is to understand one successful extraction before repeating the same pattern for the other modules.

## 2. The Big Idea

A monolith usually starts like this:

```text
Client -> Monolith -> One database
```

All features are inside one application.

All data is usually inside one database.

That can work well at the beginning, but as the system grows, the monolith can become hard to change, test, scale, and deploy.

Instead of replacing everything at once, we can refactor gradually.

The idea is:

```text
Move one business capability out of the monolith.
Keep the rest of the monolith running.
Route traffic to the correct place.
Repeat the process for the next capability.
```

This gradual approach is often called the Strangler Fig pattern.

## 3. Before the Hospital Refactor

Before extraction, all APIs were served by the monolith:

```text
Client -> cmis-monolith -> cmis_db
```

The monolith handled:

1. Hospitals.
2. Members.
3. Claims.
4. Adjudications.

The monolith database was:

```text
cmis_db
```

Simple view:

```text
Client
  |
  v
cmis-monolith
  |
  v
cmis_db
```

## 4. After the Hospital Refactor

After extracting hospitals, the system looks like this:

```text
Client -> cmis-gateway
```

Then the gateway decides:

```text
/api/hospitals/**      -> hospital-service
/api/members/**        -> cmis-monolith
/api/claims/**         -> cmis-monolith
/api/adjudications/**  -> cmis-monolith
```

So the runtime picture becomes:

```text
Client
  |
  v
cmis-gateway :8080
  |
  |-- /api/hospitals/**      -> hospital-service :8081 -> hospital_db
  |
  |-- /api/members/**        -> cmis-monolith :8090 -> cmis_db
  |
  |-- /api/claims/**         -> cmis-monolith :8090 -> cmis_db
  |
  |-- /api/adjudications/**  -> cmis-monolith :8090 -> cmis_db
```

The client still uses one base URL:

```text
http://localhost:8080
```

The client does not need to know which backend application owns each endpoint.

## 5. Why We Need a Gateway

The gateway is the public entry point into the system.

Without a gateway, clients would need to know internal service addresses:

```text
Hospitals -> http://localhost:8081
Members   -> http://localhost:8090
Claims    -> http://localhost:8090
```

That is not good because the client becomes aware of our internal refactoring process.

With a gateway, the client only calls:

```text
http://localhost:8080
```

The gateway routes each request to the correct backend:

| Request to gateway | Gateway sends it to |
| --- | --- |
| `GET /api/hospitals` | `hospital-service` on `8081` |
| `POST /api/hospitals` | `hospital-service` on `8081` |
| `GET /api/members` | `cmis-monolith` on `8090` |
| `GET /api/claims` | `cmis-monolith` on `8090` |
| `POST /api/adjudications` | `cmis-monolith` on `8090` |

This is what allows partial refactoring.

We can move hospitals now, then members later, then claims later, while the client keeps using the same base URL.

## 6. Why Hospitals Are Refactored First

Hospitals are a good first example because the module is small enough to understand, but still realistic.

Hospitals have:

1. A clear API path: `/api/hospitals/**`.
2. A clear database table: `hospitals`.
3. A clear business meaning.
4. Dependencies from other areas, especially claims.

That last point is important.

Claims depend on hospital data.

So extracting hospitals helps you see a real migration challenge:

```text
What happens when one module moves, but another module that depends on it is still inside the monolith?
```

This is exactly the kind of problem you meet in real refactoring work.

## 7. The Hospital Service

The extracted hospital service is here:

```text
cmis-microservices/hospital-service
```

It owns the hospital API:

```text
GET  /api/hospitals
GET  /api/hospitals/{id}
POST /api/hospitals
```

It also has its own database:

```text
hospital_db
```

Inside the service, you will see familiar Spring Boot layers:

| Layer | Purpose |
| --- | --- |
| Controller | Receives HTTP requests |
| DTOs | Defines request and response data |
| Service | Holds business logic |
| Repository | Reads and writes database records |
| Flyway migrations | Creates and seeds database tables |

This structure is similar to the monolith, but now it is focused only on hospitals.

## 8. Do Not Delete Monolith Code Too Early

After extracting hospital-service, you may notice that the monolith still contains hospital code.

That is normal.

Do not rush to delete it.

The public Hospital API is now owned by `hospital-service`, but other parts of the monolith may still depend on old hospital data or old hospital code.

In this project, claims are still inside the monolith.

Claims still use `cmis_db`.

So the old hospital table in `cmis_db` is still needed during this stage.

The rule is:

> Move traffic first. Remove old code later, after dependent modules are migrated or safely changed.

## 9. Database Ownership

Before refactoring:

```text
cmis-monolith -> cmis_db
```

After extracting hospitals:

```text
hospital-service -> hospital_db
cmis-monolith    -> cmis_db
```

This creates a temporary transition state.

Hospital data can exist in two places:

| Database | Why it exists |
| --- | --- |
| `hospital_db` | New database owned by hospital-service |
| `cmis_db` | Old monolith database still used by claims and other legacy code |

For this lesson, both databases start with the same hospital seed data.

That allows you to test routing first before solving synchronization.

## 10. Why Claims Still Work

A common question is:

```text
If hospitals moved to hospital-service, how can claims still work?
```

The answer is:

```text
Claims have not moved yet.
```

Claims still run inside the monolith.

Claims still read from:

```text
cmis_db
```

So the monolith keeps its old hospital data for now.

During this stage:

1. New public hospital API calls go to `hospital-service`.
2. Hospital-service reads and writes `hospital_db`.
3. Claims still run in the monolith.
4. Claims still use `cmis_db`.
5. The gateway hides this transition from the client.

This is not the final architecture.

It is a safe intermediate step.

## 11. What Happens When You Create a New Hospital?

If you call:

```http
POST http://localhost:8080/api/hospitals
```

the request goes:

```text
Client -> Gateway -> hospital-service -> hospital_db
```

The new hospital is saved in `hospital_db`.

It is not automatically saved in `cmis_db`.

That means claims in the monolith will not automatically see that new hospital.

This is the main data transition issue.

Later, you can solve it using:

| Option | Meaning |
| --- | --- |
| Data synchronization | Hospital-service publishes updates and the monolith receives them |
| API lookup | Claims calls hospital-service when it needs hospital details |
| Refactor claims next | Claims moves out of the monolith and integrates with hospital-service |
| Temporary dual-write | Write to both databases during a short transition |

For this first lesson, we keep the design simple and focus on understanding the route extraction.

## 12. Current Ports

| Application | Port | Purpose |
| --- | ---: | --- |
| `cmis-gateway` | `8080` | Public entry point |
| `hospital-service` | `8081` | Extracted hospital microservice |
| `cmis-monolith` | `8090` | Legacy APIs not yet extracted |

The most important URL is:

```text
http://localhost:8080
```

That is the client-facing URL.

## 13. Swagger URLs

Use these Swagger pages:

| URL | What it shows |
| --- | --- |
| `http://localhost:8080` | Gateway Swagger selector |
| `http://localhost:8081` | Hospital-service Swagger only |
| `http://localhost:8090` | Monolith Swagger only |

For normal testing, prefer:

```text
http://localhost:8080
```

That shows the system from the client point of view.

## 14. Refactoring Steps

The hospital extraction follows these steps.

### Step 1: Find the hospital code in the monolith

Start here:

```text
cmis-monolith/src/main/java/com/zhsf/cmis/hospital
```

Identify:

1. Controller endpoints.
2. Request and response objects.
3. Service methods.
4. Repository methods.
5. Database migrations.
6. Other modules that depend on hospital data.

Before moving code, understand the boundary.

### Step 2: Create hospital-service

Create a separate Spring Boot application:

```text
cmis-microservices/hospital-service
```

Keep the public API shape familiar:

```text
GET  /api/hospitals
GET  /api/hospitals/{id}
POST /api/hospitals
```

The goal is that clients do not need to learn a new API just because the backend changed.

### Step 3: Give hospital-service its own database

The hospital service connects to:

```text
jdbc:postgresql://localhost:5432/hospital_db
```

This is important because a microservice should own its own data.

Other services should not directly write into `hospital_db`.

### Step 4: Add database migrations

The hospital service creates its table using Flyway:

```text
cmis-microservices/hospital-service/src/main/resources/db/migration
```

For this training project, the migrations also seed starter hospital data.

### Step 5: Put the gateway in front

The gateway runs on:

```text
http://localhost:8080
```

It routes:

```text
/api/hospitals/** -> http://localhost:8081
/api/**           -> http://localhost:8090
```

This allows the client to keep one base URL while the backend changes.

### Step 6: Test the routing

Prove that hospital traffic goes to hospital-service:

```powershell
Invoke-RestMethod http://localhost:8080/api/hospitals
Invoke-RestMethod http://localhost:8081/api/hospitals
```

The responses should match.

Prove that member traffic still goes to the monolith:

```powershell
Invoke-RestMethod http://localhost:8080/api/members
Invoke-RestMethod http://localhost:8090/api/members
```

The responses should match.

Prove that claims traffic still goes to the monolith:

```powershell
Invoke-RestMethod http://localhost:8080/api/claims
Invoke-RestMethod http://localhost:8090/api/claims
```

The responses should match.

## 15. When Is This Refactor Complete?

For this stage, the hospital refactor is complete when:

1. `hospital-service` runs independently.
2. `hospital-service` uses `hospital_db`.
3. `cmis-monolith` still runs the non-refactored APIs.
4. The gateway routes `/api/hospitals/**` to `hospital-service`.
5. The gateway routes other `/api/**` requests to the monolith.
6. Swagger works through the gateway.
7. You can explain why `cmis_db` still contains hospital data.
8. You can explain why claims still work.

That is enough for the first refactoring lesson.

## 16. What Would Be Needed for Production?

This training project focuses on the refactoring idea.

A production version would need more work:

1. Authentication and authorization.
2. Service-to-service security.
3. Centralized logging.
4. Metrics and tracing.
5. Data synchronization strategy.
6. Contract tests between services.
7. Deployment automation.
8. Rollback strategy.
9. Removal of old monolith code after dependent modules are migrated.

Do not confuse the training milestone with the final production architecture.

## 17. Simple Summary

Remember this:

1. We are refactoring gradually.
2. Only hospitals have moved out of the monolith.
3. The gateway is the public entry point.
4. Hospital routes go to `hospital-service`.
5. Other routes still go to `cmis-monolith`.
6. `hospital-service` owns `hospital_db`.
7. The monolith still owns `cmis_db`.
8. Claims still work because claims have not moved yet.
9. New hospital writes do not automatically appear in `cmis_db`.
10. Later, we can refactor more modules or add synchronization.

The key lesson:

> A good monolith refactor is not one big rewrite. It is a controlled sequence of small moves, with routing and data ownership handled carefully at each step.

