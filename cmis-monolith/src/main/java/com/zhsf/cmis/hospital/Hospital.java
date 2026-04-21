package com.zhsf.cmis.hospital;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "hospitals", indexes = {
        @Index(name = "idx_hospitals_code", columnList = "hospital_code", unique = true),
        @Index(name = "idx_hospitals_status", columnList = "status")
})
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_code", nullable = false, unique = true, length = 20)
    private String hospitalCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(name = "contact_email", nullable = false, length = 160)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HospitalStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
        if (status == null) status = HospitalStatus.ACTIVE;
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
    public HospitalStatus getStatus() { return status; }
    public void setStatus(HospitalStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
