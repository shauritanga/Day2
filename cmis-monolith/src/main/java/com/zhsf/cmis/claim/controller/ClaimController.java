package com.zhsf.cmis.claim.controller;

import com.zhsf.cmis.claim.ClaimResponse;
import com.zhsf.cmis.claim.CreateClaimRequest;
import com.zhsf.cmis.claim.UpdateClaimStatusRequest;
import com.zhsf.cmis.claim.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping
    public List<ClaimResponse> listAll() {
        return claimService.findAll();
    }

    @GetMapping("/{claimNumber}")
    public ClaimResponse getByClaimNumber(@PathVariable String claimNumber) {
        return claimService.findByClaimNumber(claimNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse submit(@Valid @RequestBody CreateClaimRequest request) {
        return claimService.create(request);
    }

    @PatchMapping("/{claimNumber}/status")
    public ClaimResponse updateStatus(@PathVariable String claimNumber,
                                      @Valid @RequestBody UpdateClaimStatusRequest request) {
        return claimService.updateStatus(claimNumber, request);
    }
}
