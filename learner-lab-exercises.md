# Learner Lab: Refactoring Hospital from a Monolith

In this lab, you will explore how one part of a monolith can be refactored into a microservice while the rest of the system continues running in the monolith.

The part already refactored is:

```text
Hospitals
```

The parts still running in the monolith are:

```text
Members
Claims
Adjudications
```

Your goal is not only to run the APIs. Your goal is to understand how the system is behaving after a partial refactor.

## Before You Start

Make sure these three applications are running:

| Application | URL |
| --- | --- |
| Gateway | `http://localhost:8080` |
| Hospital service | `http://localhost:8081` |
| Monolith | `http://localhost:8090` |

You can check health endpoints:

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

If one application is not `UP`, fix that first before continuing.

## Lab 1: Observe the Monolith Before Thinking About the Gateway

Open the monolith Swagger page:

```text
http://localhost:8090
```

Look for these API groups:

1. Hospitals.
2. Members.
3. Claims.
4. Adjudications.

Even though hospitals have been refactored, the monolith may still show hospital endpoints.

That is expected during a gradual migration.

Answer these questions:

| Question | Your answer |
| --- | --- |
| Which endpoint group was refactored first? |  |
| Which endpoint groups are still owned by the monolith? |  |
| Why might hospital endpoints still exist inside the monolith? |  |

Expected understanding:

The monolith is not deleted immediately. Some old code remains while the system is being migrated step by step.

## Lab 2: Use the Gateway as the Public Entry Point

Open the gateway Swagger page:

```text
http://localhost:8080
```

The gateway Swagger page should show options for:

```text
Hospital Service - refactored
Monolith - not yet refactored
```

This is the main URL clients should use.

The client does not need to know whether an endpoint is served by the monolith or by a microservice.

Answer:

| Question | Your answer |
| --- | --- |
| What is the public base URL of the system? |  |
| Why is it useful to have one public base URL? |  |

Expected understanding:

The gateway hides the internal refactoring work from clients.

## Lab 3: Prove Hospital Requests Go to Hospital-Service

Run these two commands:

```powershell
Invoke-RestMethod http://localhost:8080/api/hospitals
Invoke-RestMethod http://localhost:8081/api/hospitals
```

The first command calls the gateway.

The second command calls hospital-service directly.

Compare the results.

Answer:

| Question | Your answer |
| --- | --- |
| Do the results match? |  |
| What does that prove? |  |

Expected understanding:

If both responses match, then the gateway is routing `/api/hospitals` to `hospital-service`.

The flow is:

```text
Client -> Gateway -> hospital-service
```

## Lab 4: Prove Members Still Go to the Monolith

Run these two commands:

```powershell
Invoke-RestMethod http://localhost:8080/api/members
Invoke-RestMethod http://localhost:8090/api/members
```

The first command calls the gateway.

The second command calls the monolith directly.

Compare the results.

Answer:

| Question | Your answer |
| --- | --- |
| Do the results match? |  |
| Has the members module been refactored yet? |  |
| Which application currently owns members? |  |

Expected understanding:

Members have not been refactored yet. The gateway still sends member requests to the monolith.

The flow is:

```text
Client -> Gateway -> cmis-monolith
```

## Lab 5: Prove Claims Still Go to the Monolith

Run these two commands:

```powershell
Invoke-RestMethod http://localhost:8080/api/claims
Invoke-RestMethod http://localhost:8090/api/claims
```

Compare the results.

Answer:

| Question | Your answer |
| --- | --- |
| Do the results match? |  |
| Has the claims module been refactored yet? |  |
| Why can claims still work after hospitals were extracted? |  |

Expected understanding:

Claims still run inside the monolith. The monolith still has the legacy data it needs in `cmis_db`.

## Lab 6: Create a Hospital Through the Gateway

Open:

```text
http://localhost:8080
```

Select:

```text
Hospital Service - refactored
```

Find:

```http
POST /api/hospitals
```

Use this request body:

```json
{
  "hospitalCode": "HOSP-ZNZ-010",
  "name": "Training Hospital",
  "region": "Mjini Magharibi",
  "contactEmail": "training@example.com",
  "status": "ACTIVE"
}
```

After creating the hospital, run:

```powershell
Invoke-RestMethod http://localhost:8080/api/hospitals
Invoke-RestMethod http://localhost:8081/api/hospitals
```

Answer:

| Question | Your answer |
| --- | --- |
| Does the new hospital appear through the gateway? |  |
| Does the new hospital appear when calling hospital-service directly? |  |
| Which database stored the new hospital? |  |

Expected understanding:

The new hospital is stored by `hospital-service` in `hospital_db`.

## Lab 7: Understand the Database Transition

Now think about the new hospital you created.

It was stored in:

```text
hospital_db
```

But claims still run inside the monolith and use:

```text
cmis_db
```

Answer:

| Question | Your answer |
| --- | --- |
| Will claims automatically see the new hospital? |  |
| Why or why not? |  |
| What could we add later to solve this? |  |

Expected understanding:

Claims will not automatically see a new hospital created only in `hospital_db`. To solve this later, we can add data synchronization, make claims call hospital-service, or refactor claims next.

## Lab 8: Try the Internal Hospital Endpoint

The hospital service has an internal endpoint:

```text
/api/hospitals/internal/{id}
```

Try calling it through the gateway:

```powershell
Invoke-WebRequest http://localhost:8080/api/hospitals/internal/1
```

Answer:

| Question | Your answer |
| --- | --- |
| What status code did you receive? |  |
| Why should the gateway block this endpoint? |  |

Expected understanding:

Internal service endpoints are not automatically public APIs. The gateway should protect internal endpoints from external clients.

## Lab 9: Read the Gateway Routing Code

Open:

```text
cmis-gateway/src/main/java/com/zhsf/gateway/GatewayProxyController.java
```

Find this method:

```java
private boolean isHospitalRoute(String path) {
    return path.equals("/api/hospitals") || path.startsWith("/api/hospitals/");
}
```

Then find where it is used:

```java
String upstreamBaseUrl = isHospitalRoute(path)
        ? routeProperties.hospitalServiceUrl()
        : routeProperties.monolithUrl();
```

Answer:

| Question | Your answer |
| --- | --- |
| What happens when the path starts with `/api/hospitals`? |  |
| What happens for all other `/api/**` paths? |  |
| Why is this useful during refactoring? |  |

Expected understanding:

The gateway contains the routing decision. It sends hospital traffic to the new service and sends everything else to the monolith.

## Lab 10: Design the Next Refactor

Now choose the next module to refactor:

1. Members.
2. Claims.
3. Adjudications.

Complete this table for your chosen module:

| Design question | Your answer |
| --- | --- |
| Which module will you refactor next? |  |
| What API path will move? |  |
| What will the new service be called? |  |
| What port will it run on? |  |
| What database will it own? |  |
| Which monolith tables are involved? |  |
| Which other modules depend on this data? |  |
| What gateway route rule will you add? |  |
| How will you prove the route works? |  |

Example for members:

| Design question | Example answer |
| --- | --- |
| Module | Members |
| API path | `/api/members/**` |
| Service | `member-service` |
| Port | `8082` |
| Database | `member_db` |
| Gateway route | `/api/members/** -> http://localhost:8082` |
| Remaining monolith APIs | claims and adjudications |

Expected understanding:

The hospital refactor is a pattern. Once you understand it, you can repeat it for another module.

## Lab 11: Explain the Current Architecture

Write one paragraph explaining the current architecture.

Your paragraph should mention:

1. Gateway.
2. Hospital-service.
3. Monolith.
4. `hospital_db`.
5. `cmis_db`.
6. Which APIs are refactored.
7. Which APIs are not refactored.

Example answer:

> The gateway runs on port 8080 and is the public entry point. Hospital requests are routed to hospital-service on port 8081. Members, claims, and adjudications are still routed to the monolith on port 8090. Hospital-service uses `hospital_db`. The monolith still uses `cmis_db` because the other modules have not been refactored yet.

## Final Review Checklist

At the end of this lab, you should be able to answer yes to each question:

| Question | Yes or No |
| --- | --- |
| Can I explain why we do not refactor everything at once? |  |
| Can I explain why the gateway is needed? |  |
| Can I identify which endpoints are refactored? |  |
| Can I identify which endpoints remain in the monolith? |  |
| Can I explain why `hospital_db` and `cmis_db` both exist? |  |
| Can I run the three applications? |  |
| Can I test routing through Swagger? |  |
| Can I explain why internal endpoints are blocked? |  |
| Can I describe how to refactor the next module? |  |

## Key Lesson

The most important idea in this lab is:

> We can move one business capability out of the monolith while the rest of the monolith continues working. The gateway keeps the public API stable while the backend changes step by step.

