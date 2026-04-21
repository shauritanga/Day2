package com.zhsf.cmis.hospital;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHospitalRequest(
        @NotBlank @Size(max = 20) String hospitalCode,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 100) String region,
        @NotBlank @Email @Size(max = 160) String contactEmail
) {}
