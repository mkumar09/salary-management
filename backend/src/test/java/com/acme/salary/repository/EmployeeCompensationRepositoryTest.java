package com.acme.salary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.CompensationRecord;
import com.acme.salary.entity.Country;
import com.acme.salary.entity.Department;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/**
 * Exercises the schema/entity mapping directly against H2 (running the real Flyway migrations),
 * not the full 10k seed - fast, deterministic fixtures only.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeCompensationRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CompensationRecordRepository compensationRecordRepository;
    @Autowired
    private CurrentCompensationRepository currentCompensationRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private Employee persistEmployee(String code, String email) {
        Country country = countryRepository.findAll().stream().findFirst()
                .orElseGet(() -> countryRepository.save(
                        Country.builder().name("United States").isoCode("US").currencyCode("USD").build()));
        Department department = departmentRepository.findAll().stream().findFirst()
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Engineering").build()));

        Employee employee = Employee.builder()
                .employeeCode(code)
                .firstName("Ada")
                .lastName("Lovelace")
                .email(email)
                .department(department)
                .country(country)
                .jobTitle("Software Engineer")
                .hireDate(LocalDate.of(2023, 1, 15))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        return employeeRepository.save(employee);
    }

    @Test
    void currentCompensationView_returnsLatestRecordByEffectiveDate() {
        Employee employee = persistEmployee("EMP-0001", "ada.lovelace@example.com");

        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee)
                .amount(new BigDecimal("90000.00"))
                .currencyCode("USD")
                .effectiveDate(LocalDate.of(2023, 1, 15))
                .reason(CompensationReason.HIRE)
                .build());
        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee)
                .amount(new BigDecimal("100000.00"))
                .currencyCode("USD")
                .effectiveDate(LocalDate.of(2024, 6, 1))
                .reason(CompensationReason.RAISE)
                .build());

        Optional<com.acme.salary.entity.CurrentCompensation> current =
                currentCompensationRepository.findByEmployeeId(employee.getId());

        assertThat(current).isPresent();
        assertThat(current.get().getAmount()).isEqualByComparingTo("100000.00");
        assertThat(current.get().getReason()).isEqualTo(CompensationReason.RAISE);
    }

    @Test
    void compensationHistory_isOrderedMostRecentFirst() {
        Employee employee = persistEmployee("EMP-0002", "grace.hopper@example.com");

        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee).amount(new BigDecimal("80000.00")).currencyCode("USD")
                .effectiveDate(LocalDate.of(2022, 1, 1)).reason(CompensationReason.HIRE).build());
        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee).amount(new BigDecimal("85000.00")).currencyCode("USD")
                .effectiveDate(LocalDate.of(2023, 1, 1)).reason(CompensationReason.RAISE).build());

        List<CompensationRecord> history =
                compensationRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(employee.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getEffectiveDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(history.get(1).getEffectiveDate()).isEqualTo(LocalDate.of(2022, 1, 1));
    }

    @Test
    void deletingEmployee_cascadesToCompensationRecords() {
        Employee employee = persistEmployee("EMP-0003", "alan.turing@example.com");
        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee).amount(new BigDecimal("95000.00")).currencyCode("USD")
                .effectiveDate(LocalDate.of(2023, 1, 1)).reason(CompensationReason.HIRE).build());

        // Flush + clear so the delete below exercises the DB-level ON DELETE CASCADE (what a real
        // direct-SQL delete would rely on) rather than Hibernate's in-memory association tracking.
        testEntityManager.flush();
        testEntityManager.clear();

        employeeRepository.deleteById(employee.getId());
        employeeRepository.flush();

        assertThat(compensationRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(employee.getId()))
                .isEmpty();
    }

    @Test
    void emailUniqueness_isEnforced() {
        persistEmployee("EMP-0004", "duplicate@example.com");

        assertThat(employeeRepository.existsByEmailIgnoreCase("duplicate@example.com")).isTrue();
        assertThat(employeeRepository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }
}
