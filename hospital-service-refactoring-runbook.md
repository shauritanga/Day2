# Hospital Service Refactoring Runbook

This runbook helps you run and test the CMIS project after the Hospital API has been extracted from the monolith.

Use it when you want to prove that:

1. Hospital endpoints are now served by `hospital-service`.
2. Members, claims, and adjudications are still served by `cmis-monolith`.
3. The gateway keeps one public URL for the whole system.

## 1. What You Are Going to Run

You will run three Spring Boot applications:

| Application | Folder | Port | Purpose |
| --- | --- | ---: | --- |
| Gateway | `cmis-gateway` | `8080` | Public entry point |
| Hospital service | `cmis-microservices/hospital-service` | `8081` | Refactored Hospital API |
| Monolith | `cmis-monolith` | `8090` | APIs not yet refactored |

The most important URL is:

```text
http://localhost:8080
```

That is the URL clients should use.

The gateway decides whether each request should go to `hospital-service` or to the monolith.

## 2. Expected Routing

After all applications are running, routing should work like this:

| Client calls | Gateway forwards to |
| --- | --- |
| `GET /api/hospitals` | `hospital-service` on port `8081` |
| `POST /api/hospitals` | `hospital-service` on port `8081` |
| `GET /api/members` | `cmis-monolith` on port `8090` |
| `GET /api/claims` | `cmis-monolith` on port `8090` |
| `GET /api/adjudications/{claimNumber}` | `cmis-monolith` on port `8090` |

This is the main thing you are proving in this runbook.

## 3. Database Setup

The project uses PostgreSQL.

You need two databases:

| Database | Used by | Why |
| --- | --- | --- |
| `cmis_db` | `cmis-monolith` | Legacy monolith data |
| `hospital_db` | `hospital-service` | New hospital service data |

Hospital-service also creates an outbox table in `hospital_db`:

```text
hospital_sync_outbox
```

When a new hospital is created, hospital-service writes the hospital and an outbox sync record. A background worker pushes that record to the monolith so `cmis_db.hospitals` stays updated for legacy claims.

The application user is:

```text
cmis_user
```

The password used by the project configuration is:

```text
cmis_pass
```

### 3.1 Open PostgreSQL as Admin

In PowerShell, open `psql` as the `postgres` admin user:

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -U postgres -d postgres
```

If your PostgreSQL version is not `18`, adjust the folder path.

### 3.2 Create or Update the User

Run this SQL:

```sql
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE rolname = 'cmis_user'
   ) THEN
      CREATE USER cmis_user WITH PASSWORD 'cmis_pass';
   ELSE
      ALTER USER cmis_user WITH PASSWORD 'cmis_pass';
   END IF;
END
$$;
```

This creates `cmis_user` if it does not exist. If it already exists, it resets the password to `cmis_pass`.

### 3.3 Create the Databases

If the databases do not already exist, run:

```sql
CREATE DATABASE cmis_db OWNER cmis_user;
CREATE DATABASE hospital_db OWNER cmis_user;
```

If a database already exists, PostgreSQL will report an error for that specific `CREATE DATABASE` command. That is fine. Continue with the ownership and grant commands below.

### 3.4 Grant Permissions

Run:

```sql
GRANT ALL PRIVILEGES ON DATABASE cmis_db TO cmis_user;
GRANT ALL PRIVILEGES ON DATABASE hospital_db TO cmis_user;
```

Then grant schema permissions inside `cmis_db`:

```sql
\c cmis_db
GRANT ALL ON SCHEMA public TO cmis_user;
ALTER SCHEMA public OWNER TO cmis_user;
```

Then grant schema permissions inside `hospital_db`:

```sql
\c hospital_db
GRANT ALL ON SCHEMA public TO cmis_user;
ALTER SCHEMA public OWNER TO cmis_user;
```

You can exit `psql`:

```sql
\q
```

## 4. Build the Applications

Open PowerShell.

If `java` or `mvn` is not recognized, run this first:

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

Important Maven syntax:

```powershell
mvn -q -DskipTests package
```

The `-DskipTests` part must start with a dash.

## 5. Start the Applications

Open three separate PowerShell windows.

Start the monolith first:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-monolith
java -jar target\cmis-monolith-1.0.0.jar
```

Start hospital-service second:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-microservices\hospital-service
java -jar target\hospital-service-1.0.0.jar
```

Start the gateway last:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2\cmis-gateway
java -jar target\cmis-gateway-1.0.0.jar
```

The order is useful because the gateway forwards requests to the other applications. If the monolith or hospital-service is not running, the gateway can start, but some routed requests will fail.

## 6. Check That Everything Is Running

Open these URLs in the browser or call them from PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8090/actuator/health
```

Each one should return:

```json
{
  "status": "UP"
}
```

If one application is not `UP`, fix that application before testing routing.

## 7. Open Swagger

Open the gateway Swagger page:

```text
http://localhost:8080
```

This is the main Swagger page for testing the refactored system from the client point of view.

It should allow you to view:

```text
Hospital Service - refactored
Monolith - not yet refactored
```

You can also open direct Swagger pages:

```text
http://localhost:8081
http://localhost:8090
```

Use them to compare behavior, but remember:

```text
http://localhost:8080
```

is the public client-facing entry point.

## 8. Test Hospital Routing

Run:

```powershell
Invoke-RestMethod http://localhost:8080/api/hospitals
Invoke-RestMethod http://localhost:8081/api/hospitals
```

The responses should match.

This proves:

```text
/api/hospitals through the gateway goes to hospital-service.
```

Expected flow:

```text
Client -> Gateway :8080 -> hospital-service :8081
```

## 9. Test Member Routing

Run:

```powershell
Invoke-RestMethod http://localhost:8080/api/members
Invoke-RestMethod http://localhost:8090/api/members
```

The responses should match.

This proves:

```text
/api/members through the gateway still goes to the monolith.
```

Expected flow:

```text
Client -> Gateway :8080 -> cmis-monolith :8090
```

## 10. Test Claim Routing

Run:

```powershell
Invoke-RestMethod http://localhost:8080/api/claims
Invoke-RestMethod http://localhost:8090/api/claims
```

The responses should match.

This proves:

```text
/api/claims through the gateway still goes to the monolith.
```

Claims have not been refactored yet.

## 11. Run the Automated Verification Script

From the project root:

```powershell
cd C:\Users\mcb0168e\Desktop\Day2
.\scripts\test-hospital-refactored.ps1
```

The script checks:

1. Gateway hospital response matches hospital-service.
2. Gateway member response matches monolith.
3. Gateway claims response matches monolith.

If the script passes, your basic routing setup is correct.

## 12. Test Hospital Legacy Sync

Create a hospital through the gateway:

```powershell
$body = @{
  hospitalCode = "HOSP-ZNZ-020"
  name = "Outbox Sync Demo Hospital"
  region = "Mjini Magharibi"
  contactEmail = "outbox-demo@example.com"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/hospitals `
  -ContentType "application/json" `
  -Body $body
```

The write goes to:

```text
hospital-service -> hospital_db
```

Then wait a few seconds for the sync worker.

Check that the monolith legacy copy can also see it:

```powershell
Invoke-RestMethod http://localhost:8090/api/hospitals
```

Expected meaning:

```text
hospital-service owns the write, but cmis_db receives a temporary legacy copy for monolith claims.
```

## 13. What the Test Results Mean

If hospital responses match:

```text
Gateway hospital route is working.
```

If member responses match:

```text
Members are still served by the monolith.
```

If claim responses match:

```text
Claims are still served by the monolith.
```

Together, these prove the partial refactor:

```text
Hospital moved.
Everything else stayed.
The gateway decides where traffic goes.
```

## 14. Common Errors and Fixes

### Gateway cannot call hospital-service

Error example:

```text
I/O error on GET request for "http://localhost:8081/api/hospitals"
```

Meaning:

`hospital-service` is not running on port `8081`, or it failed during startup.

Fix:

1. Start hospital-service.
2. Check `http://localhost:8081/actuator/health`.
3. Confirm `cmis-microservices/hospital-service/src/main/resources/application.yml` uses port `8081`.

### Gateway cannot call the monolith

Error example:

```text
I/O error on GET request for "http://localhost:8090/api/members"
```

Meaning:

`cmis-monolith` is not running on port `8090`, or it failed during startup.

Fix:

1. Start the monolith.
2. Check `http://localhost:8090/actuator/health`.
3. Confirm `cmis-monolith/src/main/resources/application.yml` uses port `8090`.

### Swagger Try It Out calls the wrong port

Gateway Swagger should call:

```text
http://localhost:8080/api/...
```

It should not call:

```text
http://localhost:8081/api/...
```

or:

```text
http://localhost:8090/api/...
```

Fix:

1. Rebuild `cmis-gateway`.
2. Restart `cmis-gateway`.
3. Open `http://localhost:8080` again.

### PostgreSQL password failed

Error example:

```text
FATAL: password authentication failed for user "cmis_user"
```

Fix:

Open `psql` as admin and run:

```sql
ALTER USER cmis_user WITH PASSWORD 'cmis_pass';
```

Then restart the application that failed.

### Database does not exist

Error example:

```text
FATAL: database "cmis_db" does not exist
```

or:

```text
FATAL: database "hospital_db" does not exist
```

Fix:

Create the missing database and grant ownership to `cmis_user`.

### Hospital sync is not reaching the monolith

Symptom:

New hospital appears through:

```text
http://localhost:8080/api/hospitals
```

but does not appear in direct monolith data:

```text
http://localhost:8090/api/hospitals
```

Meaning:

The outbox worker may not have synced yet, or the monolith may be down.

Fix:

1. Wait a few seconds.
2. Confirm the monolith is running on port `8090`.
3. Check hospital-service logs for sync warnings.
4. Confirm this setting exists in hospital-service:

```yaml
legacy-sync:
  enabled: true
  monolith-base-url: http://localhost:8090
```

### Maven command is wrong

Wrong:

```powershell
mvn -q DskipTests package
```

Correct:

```powershell
mvn -q -DskipTests package
```

## 15. Review Questions

After completing the runbook, answer these:

| Question | Your answer |
| --- | --- |
| Which endpoint group has been refactored? |  |
| Which endpoint groups still run in the monolith? |  |
| Why does the client call only `localhost:8080`? |  |
| Which database does hospital-service use? |  |
| Which database does the monolith use? |  |
| Why does the monolith still have hospital data? |  |
| What table queues hospital sync records? |  |
| Why is this called eventual consistency? |  |

## 16. Final Check

You have completed this runbook when you can show:

| Check | Expected result |
| --- | --- |
| `http://localhost:8080/actuator/health` | Gateway is `UP` |
| `http://localhost:8081/actuator/health` | Hospital service is `UP` |
| `http://localhost:8090/actuator/health` | Monolith is `UP` |
| `http://localhost:8080/api/hospitals` | Data comes from hospital-service |
| `http://localhost:8080/api/members` | Data comes from monolith |
| `http://localhost:8080/api/claims` | Data comes from monolith |

The key lesson:

> The Hospital API has moved, but the whole system still works because the gateway routes each request to the correct application.
