package com.zhsf.cmis.claim;

import jakarta.validation.constraints.NotNull;

public record UpdateClaimStatusRequest(@NotNull ClaimStatus status) {}
