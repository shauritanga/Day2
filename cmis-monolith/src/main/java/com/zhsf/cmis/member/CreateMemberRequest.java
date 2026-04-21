package com.zhsf.cmis.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateMemberRequest(
        @NotBlank @Size(max = 30) String memberNumber,
        @NotBlank @Size(max = 150) String fullName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank @Size(max = 10) String gender,
        @NotBlank @Size(max = 30) String policyNumber
) {}
