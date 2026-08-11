package com.acme.salary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acme.salary.dto.CompensationRecordCreateRequest;
import com.acme.salary.dto.CompensationRecordResponse;
import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.CompensationRecord;
import com.acme.salary.entity.Country;
import com.acme.salary.entity.Department;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.exception.NotFoundException;
import com.acme.salary.repository.CompensationRecordRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.impl.CompensationServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompensationServiceImplTest {

    private CompensationRecordRepository compensationRecordRepository;
    private EmployeeRepository employeeRepository;
    private CompensationServiceImpl compensationService;

    @BeforeEach
    void setUp() {
        compensationRecordRepository = mock(CompensationRecordRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        compensationService = new CompensationServiceImpl(compensationRecordRepository, employeeRepository);
    }

    private static Employee employee(long id) {
        Employee employee = Employee.builder()
                .employeeCode("EMP-000001").firstName("Ada").lastName("Lovelace")
                .email("ada@example.com")
                .department(Department.builder().id(1L).name("Engineering").build())
                .country(Country.builder().id(1L).name("US").isoCode("US").currencyCode("USD").build())
                .jobTitle("Engineer").hireDate(LocalDate.of(2023, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE).build();
        employee.setId(id);
        return employee;
    }

    @Test
    void addRecord_rejectsHireReason() {
        CompensationRecordCreateRequest request = new CompensationRecordCreateRequest(
                new BigDecimal("50000"), "USD", LocalDate.now(), CompensationReason.HIRE);

        assertThatThrownBy(() -> compensationService.addRecord(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRecord_throwsWhenEmployeeMissing() {
        CompensationRecordCreateRequest request = new CompensationRecordCreateRequest(
                new BigDecimal("50000"), "USD", LocalDate.now(), CompensationReason.RAISE);
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compensationService.addRecord(1L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addRecord_savesRaiseForExistingEmployee() {
        Employee employee = employee(1L);
        CompensationRecordCreateRequest request = new CompensationRecordCreateRequest(
                new BigDecimal("120000.00"), "USD", LocalDate.of(2025, 3, 1), CompensationReason.RAISE);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compensationRecordRepository.save(any(CompensationRecord.class))).thenAnswer(inv -> {
            CompensationRecord record = inv.getArgument(0);
            record.setId(10L);
            return record;
        });

        CompensationRecordResponse response = compensationService.addRecord(1L, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.amount()).isEqualByComparingTo("120000.00");
        assertThat(response.reason()).isEqualTo(CompensationReason.RAISE);
    }

    @Test
    void getHistory_throwsWhenEmployeeMissing() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> compensationService.getHistory(1L)).isInstanceOf(NotFoundException.class);
    }
}
