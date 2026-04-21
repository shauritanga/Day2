package com.zhsf.cmis.claim;

import com.zhsf.cmis.adjudication.AdjudicationResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ClaimResponse(
        Long id,
        String claimNumber,
        Long hospitalId,
        String hospitalCode,
        String hospitalName,
        Long memberId,
        String memberNumber,
        String memberName,
        Instant submittedAt,
        ClaimStatus status,
        BigDecimal totalAmount,
        String notes,
        List<ClaimItemResponse> items,
        AdjudicationResponse adjudication
) {
    public static ClaimResponse from(Claim c) {
        AdjudicationResponse adj = c.getAdjudication() != null
                ? AdjudicationResponse.from(c.getAdjudication()) : null;
        return new ClaimResponse(
                c.getId(), c.getClaimNumber(),
                c.getHospital().getId(), c.getHospital().getHospitalCode(), c.getHospital().getName(),
                c.getMember().getId(), c.getMember().getMemberNumber(), c.getMember().getFullName(),
                c.getSubmittedAt(), c.getStatus(), c.getTotalAmount(), c.getNotes(),
                c.getItems().stream().map(ClaimItemResponse::from).toList(),
                adj
        );
    }
}
