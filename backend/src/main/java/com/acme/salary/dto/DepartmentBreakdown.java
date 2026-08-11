package com.acme.salary.dto;

import java.math.BigDecimal;

public record DepartmentBreakdown(
        Long departmentId,
        String departmentName,
        long headcount,
        BigDecimal totalPayrollUsd,
        BigDecimal averageSalaryUsd) {
}
