package com.zhsf.hospital.sync;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "hospital_sync_outbox", indexes = {
        @Index(name = "idx_hospital_sync_outbox_pending", columnList = "synced_at"),
        @Index(name = "idx_hospital_sync_outbox_hospital_code", columnList = "hospital_code")
})
// Temporary migration scaffolding: remove this outbox after all hospital-dependent
// monolith modules are extracted to microservices.
public class HospitalSyncOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_code", nullable = false, length = 20)
    private String hospitalCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(name = "contact_email", nullable = false, length = 160)
    private String contactEmail;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
