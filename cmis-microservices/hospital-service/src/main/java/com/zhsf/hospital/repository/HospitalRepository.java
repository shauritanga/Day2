package com.zhsf.hospital.repository;

import com.zhsf.hospital.domain.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByHospitalCode(String hospitalCode);
    boolean existsByHospitalCode(String hospitalCode);
    boolean existsByContactEmail(String contactEmail);
}
