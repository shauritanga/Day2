package com.zhsf.cmis.hospital.controller;

import com.zhsf.cmis.hospital.CreateHospitalRequest;
import com.zhsf.cmis.hospital.HospitalResponse;
import com.zhsf.cmis.hospital.LegacyHospitalSyncRequest;
import com.zhsf.cmis.hospital.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public List<HospitalResponse> listAll() {
        return hospitalService.findAll();
    }

    @GetMapping("/{id}")
    public HospitalResponse getById(@PathVariable Long id) {
        return hospitalService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse create(@Valid @RequestBody CreateHospitalRequest request) {
        return hospitalService.create(request);
    }

    @PostMapping("/internal/sync")
    public HospitalResponse syncFromHospitalService(@Valid @RequestBody LegacyHospitalSyncRequest request) {
        // Temporary migration sync endpoint: remove after all modules are extracted
        // and the monolith no longer needs a legacy hospital copy.
        return hospitalService.syncFromHospitalService(request);
    }
}
