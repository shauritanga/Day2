package com.zhsf.hospital.service;

import com.zhsf.hospital.api.request.CreateHospitalRequest;
import com.zhsf.hospital.api.response.HospitalResponse;
import com.zhsf.hospital.domain.Hospital;
import com.zhsf.hospital.exception.BusinessException;
import com.zhsf.hospital.exception.NotFoundException;
import com.zhsf.hospital.repository.HospitalRepository;
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
        return hospitalRepository.findAll().stream().map(HospitalResponse::from).toList();
    }

    public HospitalResponse findById(Long id) {
        return HospitalResponse.from(hospitalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hospital not found: " + id)));
    }

    @Transactional
    public HospitalResponse create(CreateHospitalRequest request) {
        if (hospitalRepository.existsByHospitalCode(request.hospitalCode())) {
            throw new BusinessException("Hospital code already exists: " + request.hospitalCode());
        }
        if (hospitalRepository.existsByContactEmail(request.contactEmail())) {
            throw new BusinessException("Contact email already registered: " + request.contactEmail());
        }
        Hospital h = new Hospital();
        h.setHospitalCode(request.hospitalCode().toUpperCase());
        h.setName(request.name());
        h.setRegion(request.region());
        h.setContactEmail(request.contactEmail().toLowerCase());
        return HospitalResponse.from(hospitalRepository.save(h));
    }
}
