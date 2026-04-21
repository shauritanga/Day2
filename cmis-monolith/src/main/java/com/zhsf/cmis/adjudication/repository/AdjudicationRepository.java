package com.zhsf.cmis.adjudication.repository;

import com.zhsf.cmis.adjudication.Adjudication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdjudicationRepository extends JpaRepository<Adjudication, Long> {
    Optional<Adjudication> findByClaimClaimNumber(String claimNumber);
    boolean existsByClaimId(Long claimId);
}
