package com.acme.salary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.dto.EmployeeCreateRequest;
import com.acme.salary.dto.EmployeeResponse;
import com.acme.salary.dto.EmployeeUpdateRequest;
import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.Country;
import com.acme.salary.entity.CurrentCompensation;
import com.acme.salary.entity.Department;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.exception.DuplicateResourceException;
import com.acme.salary.exception.NotFoundException;
import com.acme.salary.repository.CountryRepository;
import com.acme.salary.repository.CurrentCompensationRepository;
import com.acme.salary.repository.DepartmentRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.impl.EmployeeServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeServiceImplTest {

    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private CountryRepository countryRepository;
    private CurrentCompensationRepository currentCompensationRepository;
    private CompensationService compensationService;
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        countryRepository = mock(CountryRepository.class);
        currentCompensationRepository = mock(CurrentCompensationRepository.class);
        compensationService = mock(CompensationService.class);
        employeeService = new EmployeeServiceImpl(
                employeeRepository, departmentRepository, countryRepository,
                currentCompensationRepository, compensationService);
    }

    private static Department department() {
        return Department.builder().id(1L).name("Engineering").build();
    }

    private static Country country() {
        return Country.builder().id(1L).name("United States").isoCode("US").currencyCode("USD").build();
    }

    private static Employee sampleEmployee(long id) {
        Employee employee = Employee.builder()
                .employeeCode("EMP-000001")
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .department(department())
                .country(country())
                .jobTitle("Engineer")
                .hireDate(LocalDate.of(2023, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        employee.setId(id);
        return employee;
    }

    @Test
    void createEmployee_savesEmployeeAndRecordsHire() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Ada", "Lovelace", "ada@example.com", 1L, 1L, "Software Engineer",
                LocalDate.of(2024, 1, 10), new BigDecimal("100000.00"), "USD");

        when(employeeRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department()));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country()));
        when(employeeRepository.nextEmployeeCodeSequenceValue()).thenReturn(7L);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee employee = inv.getArgument(0);
            employee.setId(42L);
            return employee;
        });
        when(currentCompensationRepository.findByEmployeeId(42L)).thenReturn(Optional.of(
                new CurrentCompensation(42L, new BigDecimal("100000.00"), "USD",
                        LocalDate.of(2024, 1, 10), CompensationReason.HIRE)));

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response.employeeCode()).isEqualTo("EMP-000007");
        assertThat(response.currentSalaryAmount()).isEqualByComparingTo("100000.00");
        assertThat(response.currentSalaryCurrency()).isEqualTo("USD");
        verify(compensationService).recordHire(
                any(Employee.class),
                argThat(amount -> amount.compareTo(new BigDecimal("100000.00")) == 0),
                anyString());
    }

    @Test
    void createEmployee_rejectsDuplicateEmail() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Ada", "Lovelace", "ada@example.com", 1L, 1L, "Software Engineer",
                LocalDate.of(2024, 1, 10), new BigDecimal("100000.00"), "USD");
        when(employeeRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployee_throwsWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployee(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteEmployee_throwsWhenMissing() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L)).isInstanceOf(NotFoundException.class);
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void updateEmployee_rejectsEmailBelongingToAnotherEmployee() {
        Employee existing = sampleEmployee(1L);
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Ada", "Lovelace", "taken@example.com", 1L, 1L, "Engineer", EmploymentStatus.ACTIVE);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
