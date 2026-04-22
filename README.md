# CMIS Monolith to Microservices Refactoring

This project teaches you how to refactor a Spring Boot monolith into microservices gradually.

You will not refactor the whole system at once.

You will start with one business capability:

```text
Hospitals
```

The Hospital API has already been extracted into `hospital-service`. The other APIs still run inside the monolith.

## 1. What You Are Learning

By working through this project, you will learn how to:

1. Understand a monolith before refactoring it.
2. Extract one API group into a separate Spring Boot microservice.
3. Use a gateway to route traffic to the correct backend.
4. Keep non-refactored APIs running in the monolith.
5. Give the new microservice its own database.
6. Keep old monolith data synchronized during migration.
7. Prepare to refactor the next module.

## 2. Current Refactoring State

Only the Hospital API has been refactored.

| API route | Runtime owner | Database |
| --- | --- | --- |
| `/api/hospitals/**` | `hospital-service` | `hospital_db` |
| `/api/members/**` | `cmis-monolith` | `cmis_db` |
| `/api/claims/**` | `cmis-monolith` | `cmis_db` |
| `/api/adjudications/**` | `cmis-monolith` | `cmis_db` |

All clients should call the gateway:

```text
http://localhost:8080
```

The gateway decides where each request should go.

## 3. Architecture Picture

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

This is a partial refactor.

That means one module has moved, and the rest of the system still works.

## 4. Project Folders

| Folder | Purpose |
| --- | --- |
| `cmis-monolith` | Original monolith, still serving APIs that are not refactored yet |
| `cmis-gateway` | Public entry point that routes requests |
| `cmis-microservices/hospital-service` | Refactored Hospital API |
| `cmis-microservices/setup` | Database setup scripts |
| Root `*.md` files | Learning notes and lab exercises |

## 5. Applications and Ports

You will run three applications:

| Application | Port | Purpose |
| --- | ---: | --- |
| `cmis-gateway` | `8080` | Public entry point |
| `hospital-service` | `8081` | Refactored Hospital API |
| `cmis-monolith` | `8090` | APIs not refactored yet |

## 6. Databases

You need two PostgreSQL databases:

| Database | Used by |
| --- | --- |
| `cmis_db` | `cmis-monolith` |
| `hospital_db` | `hospital-service` |

The application database user is:

```text
cmis_user
```

Password:

```text
cmis_pass
```

## 7. Database Synchronization During Migration

Hospital-service owns new hospital writes:

```text
hospital-service -> hospital_db.hospitals
```

But claims still run inside the monolith and still read:

```text
cmis_db.hospitals
```

So this project includes temporary synchronization:

```text
hospital_db.hospitals
hospital_db.hospital_sync_outbox
cmis-monolith internal sync endpoint
cmis_db.hospitals
```

This means a new hospital is first saved in `hospital_db`, then copied to `cmis_db` so the old monolith can continue working.

This synchronization is temporary. After all hospital-dependent modules are refactored, it should be removed.

## 8. Build the Applications

If `java` or `mvn` is not recognized in PowerShell, run:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Build the monolith:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-monolith
mvn -q -DskipTests package
```

Build hospital-service:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-microservices\hospital-service
mvn -q -DskipTests package
```

Build the gateway:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-gateway
mvn -q -DskipTests package
```

Use:

```powershell
mvn -q -DskipTests package
```

Do not use:

```powershell
mvn -q DskipTests package
```

## 9. Run the Applications

Open three PowerShell windows.

Window 1: start the monolith.

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-monolith
java -jar target\cmis-monolith-1.0.0.jar
```

Window 2: start hospital-service.

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-microservices\hospital-service
java -jar target\hospital-service-1.0.0.jar
```

Window 3: start the gateway.

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-gateway
java -jar target\cmis-gateway-1.0.0.jar
```

## 10. Check That Everything Is Running

Run:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8090/actuator/health
```

Each should return:

```json
{
  "status": "UP"
}
```

## 11. Open Swagger

Open:

```text
http://localhost:8080
```

This is the gateway Swagger page.

You can switch between:

```text
Hospital Service - refactored
Monolith - not yet refactored
```

You can also open direct Swagger pages:

```text
http://localhost:8081
http://localhost:8090
```

Use the gateway Swagger page when you want to test from the client point of view.

## 12. Test Routing

Hospital should route to hospital-service:

```powershell
Invoke-RestMethod http://localhost:8080/api/hospitals
Invoke-RestMethod http://localhost:8081/api/hospitals
```

The responses should match.

Members should still route to the monolith:

```powershell
Invoke-RestMethod http://localhost:8080/api/members
Invoke-RestMethod http://localhost:8090/api/members
```

The responses should match.

Claims should still route to the monolith:

```powershell
Invoke-RestMethod http://localhost:8080/api/claims
Invoke-RestMethod http://localhost:8090/api/claims
```

The responses should match.

## 13. Test Hospital Synchronization

Create a hospital through the gateway:

```powershell
$body = @{
  hospitalCode = "HOSP-ZNZ-099"
  name = "Gateway Sync Test Hospital"
  region = "Mjini Magharibi"
  contactEmail = "gateway-sync-test@example.com"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/hospitals `
  -ContentType "application/json" `
  -Body $body
```

Check hospital-service:

```powershell
Invoke-RestMethod http://localhost:8081/api/hospitals |
  Where-Object { $_.hospitalCode -eq "HOSP-ZNZ-099" }
```

Wait a few seconds, then check the monolith legacy copy:

```powershell
Invoke-RestMethod http://localhost:8090/api/hospitals |
  Where-Object { $_.hospitalCode -eq "HOSP-ZNZ-099" }
```

Expected result:

```text
The hospital appears in hospital-service first, then appears in the monolith after sync.
```

## 14. Learning Documents

Start with:

[refactoring-lesson-index.md](refactoring-lesson-index.md)

Then read:

1. [monolith-to-microservices-training-notes.md](monolith-to-microservices-training-notes.md)
2. [hospital-service-refactoring-runbook.md](hospital-service-refactoring-runbook.md)
3. [database-transition-guide.md](database-transition-guide.md)
4. [gateway-routing-guide.md](gateway-routing-guide.md)
5. [learner-lab-exercises.md](learner-lab-exercises.md)

## 15. What You Should Be Able to Explain

After working through this project, you should be able to explain:

1. Why only hospitals were refactored first.
2. Why the gateway is needed.
3. Why clients call `localhost:8080`.
4. Why the monolith still exists.
5. Why `hospital-service` has its own database.
6. Why `cmis_db` still has hospital data.
7. How the outbox sync keeps the old monolith working.
8. What should happen when the next module is refactored.

## Key Lesson

You do not need to refactor the whole monolith at once.

Move one business capability at a time, route traffic through a gateway, and handle data ownership carefully during the transition.

