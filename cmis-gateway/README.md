# CMIS Gateway

This gateway demonstrates the first step of refactoring the CMIS monolith with the strangler pattern.

Public traffic continues to use one base URL:

```text
http://localhost:8080
```

Current training routes:

```text
/api/hospitals/**      -> hospital-service on http://localhost:8081
/api/members/**        -> monolith on http://localhost:8090
/api/claims/**         -> monolith on http://localhost:8090
/api/adjudications/**  -> monolith on http://localhost:8090
```

The monolith still contains hospital code and tables for now because claims still use the old monolith database model internally. The public hospital API, however, is now owned by `hospital-service`.

Run order for the lesson:

```text
1. Start PostgreSQL and create cmis_db and hospital_db.
2. Start cmis-monolith. It now runs on port 8090.
3. Start cmis-microservices/hospital-service. It runs on port 8081.
4. Start cmis-gateway. It runs on port 8080.
5. Call all APIs through http://localhost:8080.
```

To prove the routing behavior, run:

```powershell
.\scripts\test-hospital-refactored.ps1
```

from the repository root.

Swagger UI is available at:

```text
http://localhost:8080
```

Use the dropdown to switch between:

```text
Hospital Service - refactored
Monolith - not yet refactored
```
