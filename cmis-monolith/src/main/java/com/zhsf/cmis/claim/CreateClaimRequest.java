package com.zhsf.cmis.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateClaimRequest(
        @NotNull Long hospitalId,
        @NotNull Long memberId,
        @Size(max = 500) String notes,
        @NotEmpty @Valid List<CreateClaimItemRequest> items
) {}
