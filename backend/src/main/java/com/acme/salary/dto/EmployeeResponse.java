package com.acme.salary.dto;

import com.acme.salary.entity.EmploymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        DepartmentResponse department,
        CountryResponse country,
        String jobTitle,
        LocalDate hireDate,
        EmploymentStatus employmentStatus,
        BigDecimal currentSalaryAmount,
        String currentSalaryCurrency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
