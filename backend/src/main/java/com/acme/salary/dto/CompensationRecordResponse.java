package com.acme.salary.dto;

import com.acme.salary.entity.CompensationReason;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompensationRecordResponse(
        Long id,
        BigDecimal amount,
        String currencyCode,
        LocalDate effectiveDate,
        CompensationReason reason,
        LocalDateTime createdAt) {
}
