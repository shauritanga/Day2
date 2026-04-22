# Database Transition When Refactoring a Monolith

In this lesson, we are refactoring a monolith into microservices gradually. We are not moving the whole system at once.

The first part we are extracting is the Hospital API.

That sounds simple at first:

```text
Move hospitals out of the monolith into hospital-service.
```

But the real question is:

```text
What happens to the hospital data?
```

This is one of the most important parts of monolith-to-microservice refactoring.

## 1. Before Refactoring

Before we extract anything, the monolith owns one database:

```text
cmis-monolith -> cmis_db
```

Inside `cmis_db`, we have several tables:

| Table area | Used for |
| --- | --- |
| `hospitals` | Hospital records |
| `members` | Member records |
| `claims` | Claim records |
| `claim_items` | Claim line items |
| `adjudications` | Claim decisions |

This is normal in a monolith.

The application code is together, and the database is also together.

So before refactoring, the flow is:

```text
Client -> cmis-monolith -> cmis_db
```

All APIs read and write through the monolith.

## 2. What We Want After Extracting Hospitals

After we extract the Hospital API, we want this:

```text
hospital-service -> hospital_db
```

The hospital service should own its own database.

That is a key microservice rule:

> A microservice owns its data. Other services should not directly write into that service's database.

So the new direction is:

```text
Client -> Gateway -> hospital-service -> hospital_db
```

For hospital endpoints, the monolith should no longer be the public owner.

## 3. But We Are Only Partly Refactored

Here is the important part.

We have only refactored hospitals.

We have not yet refactored:

1. Members.
2. Claims.
3. Adjudications.

Those parts still run inside the monolith.

So the system now looks like this:

```text
hospital-service -> hospital_db
cmis-monolith    -> cmis_db
```

This is called a transition state.

The system is between the old architecture and the new architecture.

## 4. Why Hospital Data Exists in Two Places

During this transition, hospital data temporarily exists in two databases:

| Database | Hospital data meaning |
| --- | --- |
| `hospital_db` | New hospital-service database |
| `cmis_db` | Old monolith database still needed by legacy modules |

At first, this can feel wrong.

You may ask:

```text
If hospitals moved to hospital-service, why does cmis_db still have hospitals?
```

The answer is:

```text
Because claims have not moved yet.
```

The claims code is still inside the monolith. That claims code still expects hospital data to exist in `cmis_db`.

If we delete the old hospital table immediately, claims may break.

So for now:

1. Public hospital API goes to `hospital-service`.
2. Claims still run in the monolith.
3. The monolith still keeps its old hospital table for compatibility.

This is normal in gradual refactoring.

## 5. Which Database Is the Source of Truth?

For the newly refactored Hospital API, the source of truth is now:

```text
hospital_db
```

That means when a client calls:

```http
POST http://localhost:8080/api/hospitals
```

the request goes:

```text
Client -> Gateway -> hospital-service -> hospital_db
```

The new hospital is saved in `hospital_db`.

In this project, hospital-service also writes a synchronization record into an outbox table:

```text
hospital_db.hospital_sync_outbox
```

A background worker reads that outbox record and sends it to the monolith internal sync endpoint.

That internal endpoint updates the legacy hospital copy in:

```text
cmis_db.hospitals
```

This is important because claims are still inside the monolith and still depend on `cmis_db`.

## 6. What Happens to Claims?

Claims are not refactored yet.

So when a client calls:

```http
GET http://localhost:8080/api/claims
```

the request goes:

```text
Client -> Gateway -> cmis-monolith -> cmis_db
```

The claims module still reads from `cmis_db`.

Therefore, claims can only see the hospital records that exist in the old monolith database.

If you create a brand new hospital in `hospital_db`, claims should know about it after the sync worker pushes the change to `cmis_db`.

That is eventual consistency.

It means the new hospital may appear first in `hospital_db`, then shortly after in the monolith legacy database.

## 7. Why We Seed Both Databases in This Lesson

For teaching, we start with the same hospital records in both databases:

```text
cmis_db.hospitals
hospital_db.hospitals
```

We do this so that learners can focus first on understanding routing and service ownership.

If both databases start with the same hospitals:

1. Hospital-service can return hospital data.
2. Claims in the monolith can still work.
3. Gateway routing can be tested clearly.
4. Learners can compare before and after behavior.

This is a learning step, not the final production design.

## 8. Example Scenario

Imagine we already have this hospital in both databases:

```text
HOSP-ZNZ-001 - Mnazi Mmoja Referral Hospital
```

Now we call:

```http
GET http://localhost:8080/api/hospitals
```

The gateway routes the request to:

```text
hospital-service
```

The hospital comes from:

```text
hospital_db
```

Now we call:

```http
GET http://localhost:8080/api/claims
```

The gateway routes the request to:

```text
cmis-monolith
```

The claim data comes from:

```text
cmis_db
```

This works because both databases already contain the starter hospital data.

## 9. Example: Creating a New Hospital

Now suppose we create a new hospital:

```json
{
  "hospitalCode": "HOSP-ZNZ-010",
  "name": "Training Hospital",
  "region": "Mjini Magharibi",
  "contactEmail": "training@example.com",
  "status": "ACTIVE"
}
```

We send it to:

```http
POST http://localhost:8080/api/hospitals
```

The gateway sends it to:

```text
hospital-service
```

The new hospital is stored in:

```text
hospital_db.hospitals
```

Hospital-service also queues a sync record in:

```text
hospital_db.hospital_sync_outbox
```

Then the background sync worker sends the change to the monolith internal endpoint:

```text
POST http://localhost:8090/api/hospitals/internal/sync
```

The monolith updates its legacy copy:

```text
cmis_db.hospitals
```

So the old claims module can continue using `cmis_db` while the system is being refactored.

## 10. How Can We Solve That Later?

There are several real ways to handle this.

### Option 1: Keep the legacy copy temporarily

This is what we are doing in the first lesson.

The monolith keeps old hospital data so claims can continue working.

This is simple and good for learning.

The limitation is that new hospital data is not automatically shared with the monolith.

### Option 2: Synchronize data

When hospital-service creates or updates a hospital, it can publish an event such as:

```text
HospitalCreated
HospitalUpdated
HospitalSuspended
```

The monolith can listen to those events and update its old hospital table.

This is common in production microservices.

It gives us eventual consistency.

That means data may not update everywhere at the exact same millisecond, but it becomes consistent after the event is processed.

This project implements a simple version of this idea using an outbox table and a background sync worker.

The flow is:

```text
hospital-service saves hospital
hospital-service writes outbox record
sync worker sends record to monolith
monolith updates cmis_db.hospitals
```

This is simpler than Kafka or CDC, but it teaches the same migration idea.

### Option 3: Let the monolith call hospital-service

The claims module can call hospital-service when it needs hospital details.

The flow would be:

```text
Claims inside monolith -> hospital-service -> hospital_db
```

This avoids copying some data.

But it also means claims depends on hospital-service being available.

If hospital-service is down, claims may be affected.

### Option 4: Refactor claims next

Another good path is to refactor claims after hospitals.

Then claims can become its own service and integrate with hospital-service properly.

For example:

```text
claims-service -> hospital-service
claims-service -> claims_db
hospital-service -> hospital_db
```

This is usually a bigger step because claims often has more business rules than hospitals.

## 11. What We Should Not Do

We should not immediately delete the old hospital table from `cmis_db`.

That would be dangerous because claims still depends on the monolith database structure.

We should also avoid letting every service write directly to every other service's database.

For example, this is not a good long-term design:

```text
claims-service -> hospital_db
member-service -> hospital_db
adjudication-service -> hospital_db
```

That creates database coupling.

If many services write directly to `hospital_db`, then hospital-service no longer truly owns its data.

## 12. The Correct Mental Model

Think about the refactor in stages.

Stage 1: Original monolith

```text
Client -> cmis-monolith -> cmis_db
```

Stage 2: Hospital API extracted

```text
Client -> Gateway -> hospital-service -> hospital_db
Client -> Gateway -> cmis-monolith -> cmis_db
```

Stage 3: More services extracted later

```text
Client -> Gateway -> hospital-service
Client -> Gateway -> member-service
Client -> Gateway -> claims-service
Client -> Gateway -> adjudication-service
```

We are currently in Stage 2.

That is why the system has both a monolith and a microservice.

## 13. Simple Summary

Remember this:

1. Before refactoring, all data is in `cmis_db`.
2. After extracting hospitals, hospital-service owns `hospital_db`.
3. The monolith still owns `cmis_db`.
4. Claims still use `cmis_db` because claims are not refactored yet.
5. New hospital writes go to `hospital_db`.
6. Hospital-service queues a sync record in `hospital_sync_outbox`.
7. The sync worker updates the legacy hospital copy in `cmis_db`.
8. Later, claims can be refactored so the legacy copy is no longer needed.

The key lesson:

> In microservice refactoring, moving the API is only half of the work. You must also plan how data ownership changes over time.
