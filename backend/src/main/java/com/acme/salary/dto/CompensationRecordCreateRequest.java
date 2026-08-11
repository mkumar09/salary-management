package com.acme.salary.dto;

import com.acme.salary.entity.CompensationReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reason is restricted to RAISE/ADJUSTMENT at the API boundary - HIRE is only ever created
 * internally as part of employee creation (see CompensationServiceImpl.recordHire).
 */
public record CompensationRecordCreateRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull @Pattern(regexp = "[A-Z]{3}") String currencyCode,
        @NotNull LocalDate effectiveDate,
        @NotNull CompensationReason reason) {
}
