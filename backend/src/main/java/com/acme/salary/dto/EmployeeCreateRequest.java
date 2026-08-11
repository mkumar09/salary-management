package com.acme.salary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeCreateRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Long departmentId,
        @NotNull Long countryId,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotNull LocalDate hireDate,
        @NotNull @DecimalMin(value = "0.01") BigDecimal initialSalaryAmount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String initialSalaryCurrency) {
}
