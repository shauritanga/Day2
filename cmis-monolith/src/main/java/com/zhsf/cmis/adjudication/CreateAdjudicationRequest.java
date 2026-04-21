package com.zhsf.cmis.adjudication;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAdjudicationRequest(
        @NotBlank String claimNumber,
        @NotBlank @Size(max = 100) String reviewedBy,
        @NotNull AdjudicationDecision decision,
        @NotNull @DecimalMin("0.00") BigDecimal approvedAmount,
        @Size(max = 500) String rejectionReason
) {}
