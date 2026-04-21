package com.zhsf.hospital.api.controller;

import com.zhsf.hospital.api.request.CreateHospitalRequest;
import com.zhsf.hospital.api.response.HospitalResponse;
import com.zhsf.hospital.service.HospitalService;
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
    public List<HospitalResponse> listAll() { return hospitalService.findAll(); }

    @GetMapping("/{id}")
    public HospitalResponse getById(@PathVariable Long id) { return hospitalService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse create(@Valid @RequestBody CreateHospitalRequest request) {
        return hospitalService.create(request);
    }

    // Internal endpoint — only accessible within the cluster, blocked at API Gateway for external callers
    @GetMapping("/internal/{id}")
    public HospitalResponse getByIdInternal(@PathVariable Long id) {
        return hospitalService.findById(id);
    }
}
