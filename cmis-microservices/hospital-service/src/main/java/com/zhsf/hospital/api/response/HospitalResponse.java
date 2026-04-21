package com.zhsf.hospital.api.response;

import com.zhsf.hospital.domain.Hospital;
import com.zhsf.hospital.domain.HospitalStatus;

import java.time.Instant;

public record HospitalResponse(
        Long id, String hospitalCode, String name,
        String region, String contactEmail, HospitalStatus status, Instant createdAt
) {
    public static HospitalResponse from(Hospital h) {
        return new HospitalResponse(h.getId(), h.getHospitalCode(), h.getName(),
                h.getRegion(), h.getContactEmail(), h.getStatus(), h.getCreatedAt());
    }
}
