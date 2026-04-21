package com.zhsf.cmis.adjudication;

import java.math.BigDecimal;
import java.time.Instant;

public record AdjudicationResponse(
        Long id,
        String claimNumber,
        String reviewedBy,
        Instant reviewedAt,
        AdjudicationDecision decision,
        BigDecimal approvedAmount,
        String rejectionReason
) {
    public static AdjudicationResponse from(Adjudication a) {
        return new AdjudicationResponse(a.getId(), a.getClaim().getClaimNumber(),
                a.getReviewedBy(), a.getReviewedAt(), a.getDecision(),
                a.getApprovedAmount(), a.getRejectionReason());
    }
}
