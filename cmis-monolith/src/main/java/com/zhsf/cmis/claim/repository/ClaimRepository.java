package com.zhsf.cmis.claim.repository;

import com.zhsf.cmis.claim.Claim;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    boolean existsByClaimNumber(String claimNumber);

    @EntityGraph(attributePaths = {"hospital", "member", "items"})
    List<Claim> findAllByOrderBySubmittedAtDesc();

    @EntityGraph(attributePaths = {"hospital", "member", "items", "adjudication"})
    Optional<Claim> findByClaimNumber(String claimNumber);
}
