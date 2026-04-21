package com.zhsf.cmis.claim;

import java.math.BigDecimal;

public record ClaimItemResponse(
        Long id,
        String diagnosisCode,
        String procedureCode,
        String description,
        int quantity,
        BigDecimal unitCost,
        BigDecimal totalCost
) {
    public static ClaimItemResponse from(ClaimItem i) {
        return new ClaimItemResponse(i.getId(), i.getDiagnosisCode(), i.getProcedureCode(),
                i.getDescription(), i.getQuantity(), i.getUnitCost(), i.getTotalCost());
    }
}
