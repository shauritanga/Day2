# Refactoring Lesson Index

Start here if you are learning how to refactor the CMIS monolith into microservices.

In this lesson, only one business capability has been extracted:

```text
Hospitals
```

The other capabilities still run inside the monolith:

```text
Members
Claims
Adjudications
```

The goal is to understand how a system can move from monolith to microservices step by step without breaking the whole application.

## 1. Recommended Reading Order

Follow the files in this order.

### 1. Read the main training notes

[monolith-to-microservices-training-notes.md](monolith-to-microservices-training-notes.md)

Start here to understand the main idea.

You will learn:

1. What the CMIS project is.
2. Why we refactor gradually.
3. Why only hospitals are extracted first.
4. Why a gateway is needed.
5. Why the monolith still exists.
6. Why database ownership matters.

### 2. Run the project

[hospital-service-refactoring-runbook.md](hospital-service-refactoring-runbook.md)

Use this when you are ready to run the applications.

You will learn how to:

1. Prepare the databases.
2. Build the applications.
3. Start the monolith, hospital-service, and gateway.
4. Open Swagger.
5. Test that routing works.
6. Fix common startup and routing errors.

### 3. Understand the database transition

[database-transition-guide.md](database-transition-guide.md)

Read this carefully because the database transition is one of the most important parts of the lesson.

You will learn:

1. Why `hospital_db` exists.
2. Why `cmis_db` still exists.
3. Why hospital data can temporarily exist in two places.
4. Why claims still use the monolith database.
5. What happens when a new hospital is created.
6. How synchronization or future refactoring can solve the data gap.

### 4. Understand gateway routing

[gateway-routing-guide.md](gateway-routing-guide.md)

Read this to understand how one public URL can route requests to different backends.

You will learn:

1. Why the gateway runs on port `8080`.
2. Why hospital requests go to `hospital-service`.
3. Why member, claim, and adjudication requests still go to the monolith.
4. How the gateway routing code works.
5. Why internal endpoints are protected.
6. How the routing pattern can be repeated for the next service.

### 5. Complete the hands-on lab

[learner-lab-exercises.md](learner-lab-exercises.md)

Use this file to practice what you learned.

You will:

1. Check that all applications are running.
2. Compare gateway responses with direct service responses.
3. Prove that hospital traffic goes to hospital-service.
4. Prove that members and claims still go to the monolith.
5. Create a hospital through the gateway.
6. Think through the database impact.
7. Design the next possible refactor.

## 2. What You Should Understand by the End

By the end of this lesson, you should be able to explain this runtime state:

| API route | Runtime owner | Database |
| --- | --- | --- |
| `/api/hospitals/**` | `hospital-service` | `hospital_db` |
| `/api/members/**` | `cmis-monolith` | `cmis_db` |
| `/api/claims/**` | `cmis-monolith` | `cmis_db` |
| `/api/adjudications/**` | `cmis-monolith` | `cmis_db` |

You should also be able to explain why the client still uses one public base URL:

```text
http://localhost:8080
```

## 3. Main Architecture Idea

The current refactored state is:

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

That means:

1. One module has moved.
2. Other modules have not moved yet.
3. The system still runs.
4. The gateway keeps the public API stable.

## 4. Key Lesson

The most important lesson is:

> You do not need to refactor the whole monolith at once. You can move one business capability at a time, route traffic through a gateway, and handle data ownership carefully during the transition.

