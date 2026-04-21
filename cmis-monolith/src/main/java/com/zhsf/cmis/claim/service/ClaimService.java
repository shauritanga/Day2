package com.zhsf.cmis.claim.service;

import com.zhsf.cmis.claim.Claim;
import com.zhsf.cmis.claim.ClaimItem;
import com.zhsf.cmis.claim.ClaimResponse;
import com.zhsf.cmis.claim.ClaimStatus;
import com.zhsf.cmis.claim.CreateClaimItemRequest;
import com.zhsf.cmis.claim.CreateClaimRequest;
import com.zhsf.cmis.claim.UpdateClaimStatusRequest;
import com.zhsf.cmis.claim.repository.ClaimRepository;
import com.zhsf.cmis.hospital.Hospital;
import com.zhsf.cmis.hospital.HospitalStatus;
import com.zhsf.cmis.hospital.repository.HospitalRepository;
import com.zhsf.cmis.member.Member;
import com.zhsf.cmis.member.MemberStatus;
import com.zhsf.cmis.member.repository.MemberRepository;
import com.zhsf.cmis.shared.exception.BusinessException;
import com.zhsf.cmis.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(readOnly = true)
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final HospitalRepository hospitalRepository;
    private final MemberRepository memberRepository;

    // In production, use a database sequence — this is for teaching only
    private final AtomicInteger dailyCounter = new AtomicInteger(0);

    public ClaimService(ClaimRepository claimRepository,
                        HospitalRepository hospitalRepository,
                        MemberRepository memberRepository) {
        this.claimRepository = claimRepository;
        this.hospitalRepository = hospitalRepository;
        this.memberRepository = memberRepository;
    }

    public List<ClaimResponse> findAll() {
        return claimRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(ClaimResponse::from)
                .toList();
    }

    public ClaimResponse findByClaimNumber(String claimNumber) {
        return ClaimResponse.from(claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new NotFoundException("Claim not found: " + claimNumber)));
    }

    @Transactional
    public ClaimResponse create(CreateClaimRequest request) {
        Hospital hospital = hospitalRepository.findById(request.hospitalId())
                .orElseThrow(() -> new NotFoundException("Hospital not found with id: " + request.hospitalId()));
        if (hospital.getStatus() == HospitalStatus.SUSPENDED) {
            throw new BusinessException("Hospital is suspended and cannot submit claims: " + hospital.getHospitalCode());
        }

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + request.memberId()));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("Member is not active: " + member.getMemberNumber());
        }

        Claim claim = new Claim();
        claim.setClaimNumber(generateClaimNumber());
        claim.setHospital(hospital);
        claim.setMember(member);
        claim.setNotes(request.notes());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateClaimItemRequest itemReq : request.items()) {
            ClaimItem item = new ClaimItem();
            item.setClaim(claim);
            item.setDiagnosisCode(itemReq.diagnosisCode());
            item.setProcedureCode(itemReq.procedureCode());
            item.setDescription(itemReq.description());
            item.setQuantity(itemReq.quantity());
            item.setUnitCost(itemReq.unitCost());
            BigDecimal lineCost = itemReq.unitCost().multiply(BigDecimal.valueOf(itemReq.quantity()));
            item.setTotalCost(lineCost);
            total = total.add(lineCost);
            claim.getItems().add(item);
        }
        claim.setTotalAmount(total);

        return ClaimResponse.from(claimRepository.save(claim));
    }

    @Transactional
    public ClaimResponse updateStatus(String claimNumber, UpdateClaimStatusRequest request) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new NotFoundException("Claim not found: " + claimNumber));

        ClaimStatus current = claim.getStatus();
        if (current == ClaimStatus.APPROVED || current == ClaimStatus.REJECTED || current == ClaimStatus.PARTIALLY_APPROVED) {
            throw new BusinessException("Claim " + claimNumber + " is already finalized with status: " + current);
        }
        claim.setStatus(request.status());
        return ClaimResponse.from(claimRepository.save(claim));
    }

    private String generateClaimNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = dailyCounter.incrementAndGet();
        return String.format("CLM-%s-%05d", date, seq);
    }
}
