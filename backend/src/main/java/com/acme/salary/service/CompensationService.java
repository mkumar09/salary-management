package com.acme.salary.service;

import com.acme.salary.dto.CompensationRecordCreateRequest;
import com.acme.salary.dto.CompensationRecordResponse;
import com.acme.salary.entity.Employee;
import java.math.BigDecimal;
import java.util.List;

public interface CompensationService {

    /** Creates the initial HIRE record for a brand-new employee. Internal use only. */
    void recordHire(Employee employee, BigDecimal amount, String currencyCode);

    List<CompensationRecordResponse> getHistory(Long employeeId);

    CompensationRecordResponse addRecord(Long employeeId, CompensationRecordCreateRequest request);
}
