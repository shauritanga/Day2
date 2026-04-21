package com.zhsf.cmis.member.repository;

import com.zhsf.cmis.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberNumber(String memberNumber);
    boolean existsByMemberNumber(String memberNumber);
    boolean existsByPolicyNumber(String policyNumber);
}
