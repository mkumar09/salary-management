package com.acme.salary.service.impl;

import com.acme.salary.dto.CompensationRecordCreateRequest;
import com.acme.salary.dto.CompensationRecordResponse;
import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.CompensationRecord;
import com.acme.salary.entity.Employee;
import com.acme.salary.exception.NotFoundException;
import com.acme.salary.repository.CompensationRecordRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.CompensationService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompensationServiceImpl implements CompensationService {

    private final CompensationRecordRepository compensationRecordRepository;
    private final EmployeeRepository employeeRepository;

    public CompensationServiceImpl(
            CompensationRecordRepository compensationRecordRepository, EmployeeRepository employeeRepository) {
        this.compensationRecordRepository = compensationRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void recordHire(Employee employee, BigDecimal amount, String currencyCode) {
        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee)
                .amount(amount)
                .currencyCode(currencyCode)
                .effectiveDate(employee.getHireDate())
                .reason(CompensationReason.HIRE)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensationRecordResponse> getHistory(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new NotFoundException("Employee not found: " + employeeId);
        }
        return compensationRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(employeeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CompensationRecordResponse addRecord(Long employeeId, CompensationRecordCreateRequest request) {
        if (request.reason() == CompensationReason.HIRE) {
            throw new IllegalArgumentException("HIRE records are created automatically when an employee is added");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));

        CompensationRecord record = compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee)
                .amount(request.amount())
                .currencyCode(request.currencyCode())
                .effectiveDate(request.effectiveDate())
                .reason(request.reason())
                .build());
        return toResponse(record);
    }

    private CompensationRecordResponse toResponse(CompensationRecord record) {
        return new CompensationRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getCurrencyCode(),
                record.getEffectiveDate(),
                record.getReason(),
                record.getCreatedAt());
    }
}
