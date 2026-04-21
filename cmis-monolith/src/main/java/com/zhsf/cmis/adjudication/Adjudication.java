package com.zhsf.cmis.adjudication;

import com.zhsf.cmis.claim.Claim;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "adjudications", indexes = {
        @Index(name = "idx_adjudications_claim", columnList = "claim_id", unique = true)
})
public class Adjudication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @Column(name = "reviewed_by", nullable = false, length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdjudicationDecision decision;

    @Column(name = "approved_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @PrePersist
    private void prePersist() {
        reviewedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public AdjudicationDecision getDecision() { return decision; }
    public void setDecision(AdjudicationDecision decision) { this.decision = decision; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
