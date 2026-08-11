package com.acme.salary.dto;

import java.math.BigDecimal;

public record CountryBreakdown(
        Long countryId,
        String countryName,
        String currencyCode,
        long headcount,
        BigDecimal totalPayrollUsd,
        BigDecimal averageSalaryUsd) {
}
