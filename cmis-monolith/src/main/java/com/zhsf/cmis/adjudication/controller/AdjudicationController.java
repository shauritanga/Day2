package com.zhsf.cmis.adjudication.controller;

import com.zhsf.cmis.adjudication.AdjudicationResponse;
import com.zhsf.cmis.adjudication.CreateAdjudicationRequest;
import com.zhsf.cmis.adjudication.service.AdjudicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adjudications")
public class AdjudicationController {

    private final AdjudicationService adjudicationService;

    public AdjudicationController(AdjudicationService adjudicationService) {
        this.adjudicationService = adjudicationService;
    }

    @GetMapping("/{claimNumber}")
    public AdjudicationResponse getByClaimNumber(@PathVariable String claimNumber) {
        return adjudicationService.findByClaimNumber(claimNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdjudicationResponse create(@Valid @RequestBody CreateAdjudicationRequest request) {
        return adjudicationService.create(request);
    }
}
