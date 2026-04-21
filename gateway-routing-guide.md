# Gateway Routing During Monolith Refactoring

In this lesson, we are not replacing the whole monolith at once.

We are extracting only one business capability first:

```text
Hospitals
```

The rest of the system still runs in the monolith:

```text
Members
Claims
Adjudications
```

This creates an important question:

```text
How does the client know where to send each request?
```

The answer is:

```text
The client does not need to know.
```

The gateway knows.

## 1. The Problem Without a Gateway

Imagine we do not have a gateway.

Then the client would need to call different applications directly:

```text
Hospital requests -> http://localhost:8081
Member requests   -> http://localhost:8090
Claim requests    -> http://localhost:8090
```

That is not a good client experience.

It also exposes the internal refactoring work to the outside world.

The client would need to know:

1. Which module has already been refactored.
2. Which module is still in the monolith.
3. Which port each service uses.
4. When the next service moves.

That is too much knowledge for the client.

## 2. The Gateway Gives One Public Address

The gateway solves this by giving the whole system one public entry point:

```text
http://localhost:8080
```

So the client calls:

```text
http://localhost:8080/api/hospitals
http://localhost:8080/api/members
http://localhost:8080/api/claims
http://localhost:8080/api/adjudications
```

The client does not call the monolith or hospital-service directly.

The gateway receives the request first, then forwards it to the correct backend.

## 3. Current Runtime Picture

The system currently runs like this:

```text
Client -> cmis-gateway :8080
```

Then the gateway chooses:

```text
/api/hospitals/**      -> hospital-service :8081
/api/members/**        -> cmis-monolith :8090
/api/claims/**         -> cmis-monolith :8090
/api/adjudications/**  -> cmis-monolith :8090
```

So the gateway is the public front door.

The monolith and microservice sit behind it.

## 4. Current Routing Rules

These are the routing rules in this project:

| Client calls gateway URL | Gateway forwards to |
| --- | --- |
| `/api/hospitals` | `hospital-service` |
| `/api/hospitals/{id}` | `hospital-service` |
| `/api/members/**` | `cmis-monolith` |
| `/api/claims/**` | `cmis-monolith` |
| `/api/adjudications/**` | `cmis-monolith` |
| Any other `/api/**` request | `cmis-monolith` |

This means hospital has been refactored, but the other APIs have not.

## 5. Gateway Configuration

The gateway needs to know where the backend applications are running.

That configuration is in:

```text
cmis-gateway/src/main/resources/application.yml
```

Important part:

```yaml
server:
  port: 8080

gateway:
  routes:
    hospital-service-url: http://localhost:8081
    monolith-url: http://localhost:8090
```

Read this as:

```text
The gateway runs on port 8080.
Hospital-service runs on port 8081.
The monolith runs on port 8090.
```

## 6. How the Gateway Decides

Open this file:

```text
cmis-gateway/src/main/java/com/zhsf/gateway/GatewayProxyController.java
```

The most important routing method is:

```java
private boolean isHospitalRoute(String path) {
    return path.equals("/api/hospitals") || path.startsWith("/api/hospitals/");
}
```

This method asks a simple question:

```text
Is this request for hospitals?
```

If the answer is yes, the gateway sends the request to hospital-service.

If the answer is no, the gateway sends the request to the monolith.

The code that makes that decision is:

```java
String upstreamBaseUrl = isHospitalRoute(path)
        ? routeProperties.hospitalServiceUrl()
        : routeProperties.monolithUrl();
```

In plain language:

```text
If path is /api/hospitals or starts with /api/hospitals/,
send it to hospital-service.

Otherwise,
send it to the monolith.
```

That small decision is what makes partial refactoring possible.

## 7. Example: Hospital Request

Suppose the client calls:

```http
GET http://localhost:8080/api/hospitals
```

The gateway receives:

```text
/api/hospitals
```

The gateway checks:

```text
Is this a hospital route?
```

Yes.

So it forwards the request to:

```text
http://localhost:8081/api/hospitals
```

The response comes back from hospital-service, then the gateway returns it to the client.

The full flow is:

```text
Client -> Gateway -> hospital-service -> Gateway -> Client
```

## 8. Example: Claims Request

Now suppose the client calls:

```http
GET http://localhost:8080/api/claims
```

The gateway receives:

```text
/api/claims
```

The gateway checks:

```text
Is this a hospital route?
```

No.

So it forwards the request to:

```text
http://localhost:8090/api/claims
```

The full flow is:

```text
Client -> Gateway -> cmis-monolith -> Gateway -> Client
```

This is why claims still work even though hospitals have been extracted.

## 9. The Gateway Does Not Contain Business Logic

The gateway should not decide claim rules.

The gateway should not calculate hospital status.

The gateway should not save member data.

Its job is mainly:

1. Receive the request.
2. Decide where it should go.
3. Forward the request.
4. Return the response.

In this project, the gateway is intentionally simple so learners can clearly see the routing idea.

## 10. Protecting Internal Endpoints

The hospital service has this endpoint:

```text
/api/hospitals/internal/{id}
```

This endpoint is intended for backend service-to-service communication.

For example, in a later lesson, `claims-service` may need to ask `hospital-service` for hospital details.

But an internal endpoint should not automatically become public.

So the gateway blocks it:

```java
if (path.startsWith("/api/hospitals/internal/")) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

That means this should be blocked:

```text
http://localhost:8080/api/hospitals/internal/1
```

This teaches an important microservice idea:

> Not every endpoint inside a service should be exposed to external clients.

## 11. Swagger Through the Gateway

Swagger is also available through the gateway:

```text
http://localhost:8080
```

The gateway Swagger page lets you view:

1. Hospital Service - refactored.
2. Monolith - not yet refactored.

The gateway gets OpenAPI documentation from the backend applications:

| Gateway docs URL | Backend docs URL |
| --- | --- |
| `/docs/hospital/v3/api-docs` | `http://localhost:8081/v3/api-docs` |
| `/docs/monolith/v3/api-docs` | `http://localhost:8090/v3/api-docs` |

The important detail is that Swagger "Try it out" should call the gateway:

```text
http://localhost:8080/api/...
```

It should not call the internal service ports directly:

```text
http://localhost:8081/api/...
http://localhost:8090/api/...
```

That is why the gateway rewrites the OpenAPI `servers` value to:

```text
http://localhost:8080
```

## 12. How to Prove Routing Works

To prove hospital requests go to hospital-service, compare:

```powershell
Invoke-RestMethod http://localhost:8080/api/hospitals
Invoke-RestMethod http://localhost:8081/api/hospitals
```

The responses should match.

To prove member requests still go to the monolith, compare:

```powershell
Invoke-RestMethod http://localhost:8080/api/members
Invoke-RestMethod http://localhost:8090/api/members
```

The responses should match.

To prove claims still go to the monolith, compare:

```powershell
Invoke-RestMethod http://localhost:8080/api/claims
Invoke-RestMethod http://localhost:8090/api/claims
```

The responses should match.

## 13. What Happens When We Refactor Members Later?

Right now, member requests go to the monolith:

```text
/api/members/** -> cmis-monolith
```

When we refactor members, we will add a member service:

```text
member-service :8082
```

Then the gateway routing will change to:

```text
/api/hospitals/**      -> hospital-service :8081
/api/members/**        -> member-service :8082
/api/claims/**         -> cmis-monolith :8090
/api/adjudications/**  -> cmis-monolith :8090
```

The client still calls:

```text
http://localhost:8080/api/members
```

The client does not need to know that members moved.

That is the power of the gateway in gradual refactoring.

## 14. What Happens When We Refactor Claims Later?

Later, if claims are extracted, the gateway can route:

```text
/api/claims/** -> claims-service
```

Then the monolith becomes smaller.

The long-term direction is:

```text
Gateway -> hospital-service
Gateway -> member-service
Gateway -> claims-service
Gateway -> adjudication-service
```

At each step, the public base URL remains:

```text
http://localhost:8080
```

## 15. Simple Summary

Remember this:

1. The gateway is the public entry point.
2. Clients call `http://localhost:8080`.
3. Hospital routes go to `hospital-service`.
4. Non-hospital routes still go to the monolith.
5. The gateway does not contain business logic.
6. The gateway protects internal endpoints from public access.
7. Swagger through the gateway should call the gateway URL, not internal service ports.
8. As more modules are refactored, we add more route rules.

The key lesson:

> The gateway lets us change the backend gradually while keeping the public API stable.

