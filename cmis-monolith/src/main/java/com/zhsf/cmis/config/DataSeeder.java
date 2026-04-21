package com.zhsf.cmis.config;

import com.zhsf.cmis.adjudication.Adjudication;
import com.zhsf.cmis.adjudication.AdjudicationDecision;
import com.zhsf.cmis.adjudication.repository.AdjudicationRepository;
import com.zhsf.cmis.claim.Claim;
import com.zhsf.cmis.claim.ClaimItem;
import com.zhsf.cmis.claim.ClaimStatus;
import com.zhsf.cmis.claim.repository.ClaimRepository;
import com.zhsf.cmis.hospital.Hospital;
import com.zhsf.cmis.hospital.repository.HospitalRepository;
import com.zhsf.cmis.member.Member;
import com.zhsf.cmis.member.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seeds a complete claims workflow for training demonstrations.
 * Flyway V6 seeds hospitals and members; this seeder creates claims and adjudications
 * so the trainer can demonstrate the full lifecycle without HTTP calls.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final HospitalRepository hospitalRepository;
    private final MemberRepository memberRepository;
    private final ClaimRepository claimRepository;
    private final AdjudicationRepository adjudicationRepository;

    public DataSeeder(HospitalRepository hospitalRepository,
                      MemberRepository memberRepository,
                      ClaimRepository claimRepository,
                      AdjudicationRepository adjudicationRepository) {
        this.hospitalRepository = hospitalRepository;
        this.memberRepository = memberRepository;
        this.claimRepository = claimRepository;
        this.adjudicationRepository = adjudicationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (claimRepository.count() > 0) {
            log.info("DataSeeder: claims already exist, skipping.");
            return;
        }

        Hospital hospital = hospitalRepository.findByHospitalCode("HOSP-ZNZ-001")
                .orElseThrow(() -> new IllegalStateException("Seed hospital not found — check Flyway V6"));
        Member member = memberRepository.findByMemberNumber("MBR-2024-0001")
                .orElseThrow(() -> new IllegalStateException("Seed member not found — check Flyway V6"));

        // Claim 1: SUBMITTED — brand new, awaiting triage
        Claim claim1 = buildClaim("CLM-20240101-00001", hospital, member,
                "Outpatient visit + lab tests", ClaimStatus.SUBMITTED);
        addItem(claim1, "J06.9", "99213", "Office visit - established patient", 1, new BigDecimal("45000"));
        addItem(claim1, "J06.9", "85025", "Complete blood count (CBC)",          1, new BigDecimal("18000"));
        claim1.setTotalAmount(new BigDecimal("63000"));
        claimRepository.save(claim1);
        log.info("DataSeeder: saved {}", claim1.getClaimNumber());

        // Claim 2: APPROVED — full lifecycle for demo
        Claim claim2 = buildClaim("CLM-20240102-00001", hospital, member,
                "Emergency admission - malaria treatment", ClaimStatus.UNDER_REVIEW);
        addItem(claim2, "B54",   "99285", "Emergency department visit",  1, new BigDecimal("120000"));
        addItem(claim2, "B54",   "96365", "IV infusion - antimalarial",  2, new BigDecimal("35000"));
        addItem(claim2, "B54",   "85025", "CBC with differential",       1, new BigDecimal("18000"));
        claim2.setTotalAmount(new BigDecimal("208000"));
        claimRepository.save(claim2);

        Adjudication adj2 = new Adjudication();
        adj2.setClaim(claim2);
        adj2.setReviewedBy("Dr. Mwanajuma Khamis");
        adj2.setDecision(AdjudicationDecision.APPROVED);
        adj2.setApprovedAmount(new BigDecimal("208000"));
        adjudicationRepository.save(adj2);
        claim2.setStatus(ClaimStatus.APPROVED);
        claimRepository.save(claim2);
        log.info("DataSeeder: saved {} (APPROVED)", claim2.getClaimNumber());

        // Claim 3: PARTIALLY_APPROVED — demonstrates partial reimbursement
        Hospital hospital2 = hospitalRepository.findByHospitalCode("HOSP-ZNZ-002").orElse(hospital);
        Member member2 = memberRepository.findByMemberNumber("MBR-2024-0002").orElse(member);
        Claim claim3 = buildClaim("CLM-20240103-00001", hospital2, member2,
                "Dental procedure", ClaimStatus.UNDER_REVIEW);
        addItem(claim3, "K02.1", "D2150", "Amalgam restoration - 2 surfaces", 1, new BigDecimal("95000"));
        addItem(claim3, "K02.1", "D0274", "4 bitewing X-rays",                1, new BigDecimal("25000"));
        claim3.setTotalAmount(new BigDecimal("120000"));
        claimRepository.save(claim3);

        Adjudication adj3 = new Adjudication();
        adj3.setClaim(claim3);
        adj3.setReviewedBy("Dr. Mwanajuma Khamis");
        adj3.setDecision(AdjudicationDecision.PARTIALLY_APPROVED);
        adj3.setApprovedAmount(new BigDecimal("95000"));
        adj3.setRejectionReason("X-ray charges exceed ZHSF schedule of benefits for dental imaging.");
        adjudicationRepository.save(adj3);
        claim3.setStatus(ClaimStatus.PARTIALLY_APPROVED);
        claimRepository.save(claim3);
        log.info("DataSeeder: saved {} (PARTIALLY_APPROVED)", claim3.getClaimNumber());

        log.info("DataSeeder: complete — 3 demo claims created.");
    }

    private Claim buildClaim(String claimNumber, Hospital hospital, Member member,
                              String notes, ClaimStatus status) {
        Claim claim = new Claim();
        claim.setClaimNumber(claimNumber);
        claim.setHospital(hospital);
        claim.setMember(member);
        claim.setNotes(notes);
        claim.setStatus(status);
        return claim;
    }

    private void addItem(Claim claim, String icd10, String cpt,
                         String description, int qty, BigDecimal unitCost) {
        ClaimItem item = new ClaimItem();
        item.setClaim(claim);
        item.setDiagnosisCode(icd10);
        item.setProcedureCode(cpt);
        item.setDescription(description);
        item.setQuantity(qty);
        item.setUnitCost(unitCost);
        item.setTotalCost(unitCost.multiply(BigDecimal.valueOf(qty)));
        claim.getItems().add(item);
    }
}
