package com.zhsf.cmis.hospital.service;

import com.zhsf.cmis.hospital.CreateHospitalRequest;
import com.zhsf.cmis.hospital.Hospital;
import com.zhsf.cmis.hospital.HospitalResponse;
import com.zhsf.cmis.hospital.HospitalStatus;
import com.zhsf.cmis.hospital.LegacyHospitalSyncRequest;
import com.zhsf.cmis.hospital.repository.HospitalRepository;
import com.zhsf.cmis.shared.exception.BusinessException;
import com.zhsf.cmis.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public List<HospitalResponse> findAll() {
        return hospitalRepository.findAll().stream()
                .map(HospitalResponse::from)
                .toList();
    }

    public HospitalResponse findById(Long id) {
        return HospitalResponse.from(hospitalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hospital not found with id: " + id)));
    }

    @Transactional
    public HospitalResponse create(CreateHospitalRequest request) {
        if (hospitalRepository.existsByHospitalCode(request.hospitalCode())) {
            throw new BusinessException("Hospital code already exists: " + request.hospitalCode());
        }
        if (hospitalRepository.existsByContactEmail(request.contactEmail())) {
            throw new BusinessException("Contact email already registered: " + request.contactEmail());
        }
        Hospital hospital = new Hospital();
        hospital.setHospitalCode(request.hospitalCode().toUpperCase());
        hospital.setName(request.name());
        hospital.setRegion(request.region());
        hospital.setContactEmail(request.contactEmail().toLowerCase());
        return HospitalResponse.from(hospitalRepository.save(hospital));
    }

    @Transactional
    public HospitalResponse syncFromHospitalService(LegacyHospitalSyncRequest request) {
        // Temporary migration endpoint logic: remove after claims and all other
        // hospital-dependent modules are extracted from the monolith.
        Hospital hospital = hospitalRepository.findByHospitalCode(request.hospitalCode().toUpperCase())
                .orElseGet(Hospital::new);

        hospital.setHospitalCode(request.hospitalCode().toUpperCase());
        hospital.setName(request.name());
        hospital.setRegion(request.region());
        hospital.setContactEmail(request.contactEmail().toLowerCase());
        hospital.setStatus(HospitalStatus.valueOf(request.status().toUpperCase()));

        return HospitalResponse.from(hospitalRepository.save(hospital));
    }
}
