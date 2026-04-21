package com.zhsf.cmis.member.service;

import com.zhsf.cmis.member.CreateMemberRequest;
import com.zhsf.cmis.member.Member;
import com.zhsf.cmis.member.MemberResponse;
import com.zhsf.cmis.member.repository.MemberRepository;
import com.zhsf.cmis.shared.exception.BusinessException;
import com.zhsf.cmis.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse findByMemberNumber(String memberNumber) {
        return MemberResponse.from(memberRepository.findByMemberNumber(memberNumber)
                .orElseThrow(() -> new NotFoundException("Member not found: " + memberNumber)));
    }

    public MemberResponse findById(Long id) {
        return MemberResponse.from(memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + id)));
    }

    @Transactional
    public MemberResponse create(CreateMemberRequest request) {
        if (memberRepository.existsByMemberNumber(request.memberNumber())) {
            throw new BusinessException("Member number already exists: " + request.memberNumber());
        }
        Member member = new Member();
        member.setMemberNumber(request.memberNumber().toUpperCase());
        member.setFullName(request.fullName());
        member.setDateOfBirth(request.dateOfBirth());
        member.setGender(request.gender());
        member.setPolicyNumber(request.policyNumber());
        return MemberResponse.from(memberRepository.save(member));
    }
}
