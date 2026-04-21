package com.zhsf.cmis.claim;

import com.zhsf.cmis.hospital.Hospital;
import com.zhsf.cmis.member.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claims", indexes = {
        @Index(name = "idx_claims_number", columnList = "claim_number", unique = true),
        @Index(name = "idx_claims_hospital", columnList = "hospital_id"),
        @Index(name = "idx_claims_member", columnList = "member_id"),
        @Index(name = "idx_claims_status", columnList = "status"),
        @Index(name = "idx_claims_submitted_at", columnList = "submitted_at")
})
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 30)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private com.zhsf.cmis.adjudication.Adjudication adjudication;

    @PrePersist
    private void prePersist() {
        submittedAt = Instant.now();
        if (status == null) status = ClaimStatus.SUBMITTED;
    }

    public Long getId() { return id; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public Instant getSubmittedAt() { return submittedAt; }
    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<ClaimItem> getItems() { return items; }
    public void setItems(List<ClaimItem> items) { this.items = items; }
    public com.zhsf.cmis.adjudication.Adjudication getAdjudication() { return adjudication; }
    public void setAdjudication(com.zhsf.cmis.adjudication.Adjudication adjudication) { this.adjudication = adjudication; }
}
