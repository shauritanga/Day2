package com.zhsf.cmis.adjudication.service;

import com.zhsf.cmis.adjudication.Adjudication;
import com.zhsf.cmis.adjudication.AdjudicationDecision;
import com.zhsf.cmis.adjudication.AdjudicationResponse;
import com.zhsf.cmis.adjudication.CreateAdjudicationRequest;
import com.zhsf.cmis.adjudication.repository.AdjudicationRepository;
import com.zhsf.cmis.claim.Claim;
import com.zhsf.cmis.claim.ClaimStatus;
import com.zhsf.cmis.claim.repository.ClaimRepository;
import com.zhsf.cmis.shared.exception.BusinessException;
import com.zhsf.cmis.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdjudicationService {

    private final AdjudicationRepository adjudicationRepository;
    private final ClaimRepository claimRepository;

    public AdjudicationService(AdjudicationRepository adjudicationRepository,
                                ClaimRepository claimRepository) {
        this.adjudicationRepository = adjudicationRepository;
        this.claimRepository = claimRepository;
    }

    public AdjudicationResponse findByClaimNumber(String claimNumber) {
        return AdjudicationResponse.from(adjudicationRepository.findByClaimClaimNumber(claimNumber)
                .orElseThrow(() -> new NotFoundException("No adjudication found for claim: " + claimNumber)));
    }

    @Transactional
    public AdjudicationResponse create(CreateAdjudicationRequest request) {
        Claim claim = claimRepository.findByClaimNumber(request.claimNumber())
                .orElseThrow(() -> new NotFoundException("Claim not found: " + request.claimNumber()));

        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new BusinessException(
                    "Claim must be in UNDER_REVIEW status to be adjudicated. Current status: " + claim.getStatus());
        }
        if (adjudicationRepository.existsByClaimId(claim.getId())) {
            throw new BusinessException("Adjudication already exists for claim: " + request.claimNumber());
        }
        if (request.approvedAmount().compareTo(claim.getTotalAmount()) > 0) {
            throw new BusinessException("Approved amount (" + request.approvedAmount() +
                    ") cannot exceed claim total (" + claim.getTotalAmount() + ")");
        }

        Adjudication adj = new Adjudication();
        adj.setClaim(claim);
        adj.setReviewedBy(request.reviewedBy());
        adj.setDecision(request.decision());
        adj.setApprovedAmount(request.approvedAmount());
        adj.setRejectionReason(request.rejectionReason());
        adjudicationRepository.save(adj);

        ClaimStatus newStatus = switch (request.decision()) {
            case APPROVED -> ClaimStatus.APPROVED;
            case PARTIALLY_APPROVED -> ClaimStatus.PARTIALLY_APPROVED;
            case REJECTED -> ClaimStatus.REJECTED;
        };
        claim.setStatus(newStatus);
        claimRepository.save(claim);

        return AdjudicationResponse.from(adj);
    }
}
