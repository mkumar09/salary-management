package com.acme.salary.dto;

import com.acme.salary.entity.EmploymentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Fields an HR Manager can edit after hire. Salary changes go through the compensation
 * history endpoint instead, not through this update - see docs/architecture.md.
 */
public record EmployeeUpdateRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Long departmentId,
        @NotNull Long countryId,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotNull EmploymentStatus employmentStatus) {
}
