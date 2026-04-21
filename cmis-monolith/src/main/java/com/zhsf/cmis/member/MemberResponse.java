package com.zhsf.cmis.member;

import java.time.LocalDate;

public record MemberResponse(
        Long id,
        String memberNumber,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String policyNumber,
        MemberStatus status
) {
    public static MemberResponse from(Member m) {
        return new MemberResponse(m.getId(), m.getMemberNumber(), m.getFullName(),
                m.getDateOfBirth(), m.getGender(), m.getPolicyNumber(), m.getStatus());
    }
}
