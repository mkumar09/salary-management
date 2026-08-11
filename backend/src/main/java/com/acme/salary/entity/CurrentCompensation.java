package com.acme.salary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/**
 * Read-only mapping onto the {@code current_compensation} SQL view (see
 * V3__current_compensation_view.sql) - the latest compensation_record per employee.
 */
@Entity
@Immutable
@Table(name = "current_compensation")
@Getter
public class CurrentCompensation {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompensationReason reason;
}
