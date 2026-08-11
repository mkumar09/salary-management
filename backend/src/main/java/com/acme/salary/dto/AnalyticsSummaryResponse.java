package com.acme.salary.dto;

import java.math.BigDecimal;

/** All monetary figures are converted to USD for cross-country comparability - see docs/architecture.md. */
public record AnalyticsSummaryResponse(
        long headcount,
        BigDecimal totalPayrollUsd,
        BigDecimal averageSalaryUsd,
        BigDecimal medianSalaryUsd) {
}
