package com.zhsf.cmis.claim;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateClaimItemRequest(
        @NotBlank @Size(max = 20) String diagnosisCode,
        @NotBlank @Size(max = 20) String procedureCode,
        @NotBlank @Size(max = 255) String description,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.01") BigDecimal unitCost
) {}
