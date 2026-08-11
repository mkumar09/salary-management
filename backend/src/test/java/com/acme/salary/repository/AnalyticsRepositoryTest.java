package com.acme.salary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.CountryBreakdown;
import com.acme.salary.dto.DepartmentBreakdown;
import com.acme.salary.dto.SalaryDistributionBucket;
import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.CompensationRecord;
import com.acme.salary.entity.Country;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * Fixed, small fixture (3 employees across 2 countries/departments) rather than the 10k seed -
 * exact expected totals are hand-computable, which is what makes this test meaningful rather than
 * just "it runs".
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AnalyticsRepository.class)
class AnalyticsRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CompensationRecordRepository compensationRecordRepository;
    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Test
    void summary_byDepartment_byCountry_and_distribution_matchHandComputedTotals() {
        Country us = countryRepository.findAll().stream()
                .filter(c -> c.getIsoCode().equals("US")).findFirst().orElseThrow();
        Country india = countryRepository.findAll().stream()
                .filter(c -> c.getIsoCode().equals("IN")).findFirst().orElseThrow();
        var engineering = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equals("Engineering")).findFirst().orElseThrow();
        var sales = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equals("Sales")).findFirst().orElseThrow();

        // US/Engineering: 100,000 USD -> 100,000 USD
        hireEmployee("a@example.com", engineering, us, new BigDecimal("100000.00"), "USD");
        // US/Sales: 60,000 USD -> 60,000 USD
        hireEmployee("b@example.com", sales, us, new BigDecimal("60000.00"), "USD");
        // India/Engineering: 830,000 INR at rate_to_usd=0.012 -> 9,960 USD
        hireEmployee("c@example.com", engineering, india, new BigDecimal("830000.00"), "INR");

        AnalyticsSummaryResponse summary = analyticsRepository.summary();
        assertThat(summary.headcount()).isEqualTo(3);
        // total = 100000 + 60000 + 9960 = 169960
        assertThat(summary.totalPayrollUsd()).isEqualByComparingTo("169960.00000000");
        assertThat(summary.averageSalaryUsd().doubleValue()).isCloseTo(56653.33, org.assertj.core.data.Offset.offset(0.01));

        List<DepartmentBreakdown> byDepartment = analyticsRepository.byDepartment();
        DepartmentBreakdown engineeringBreakdown = byDepartment.stream()
                .filter(d -> d.departmentName().equals("Engineering")).findFirst().orElseThrow();
        assertThat(engineeringBreakdown.headcount()).isEqualTo(2);
        assertThat(engineeringBreakdown.totalPayrollUsd()).isEqualByComparingTo("109960.00000000");

        List<CountryBreakdown> byCountry = analyticsRepository.byCountry();
        CountryBreakdown usBreakdown = byCountry.stream()
                .filter(c -> c.countryName().equals("United States")).findFirst().orElseThrow();
        assertThat(usBreakdown.headcount()).isEqualTo(2);
        assertThat(usBreakdown.totalPayrollUsd()).isEqualByComparingTo("160000.00000000");

        List<SalaryDistributionBucket> distribution = analyticsRepository.distribution();
        long totalBucketed = distribution.stream().mapToLong(SalaryDistributionBucket::count).sum();
        assertThat(totalBucketed).isEqualTo(3);
        assertThat(distribution).anySatisfy(bucket -> {
            if (bucket.bucketLabel().equals("< 50K")) {
                assertThat(bucket.count()).isEqualTo(1); // the ~9,960 USD India employee
            }
        });
    }

    @Test
    void terminatedEmployees_areExcludedFromAnalytics() {
        Country us = countryRepository.findAll().stream()
                .filter(c -> c.getIsoCode().equals("US")).findFirst().orElseThrow();
        var engineering = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equals("Engineering")).findFirst().orElseThrow();

        Employee terminated = hireEmployee("terminated@example.com", engineering, us, new BigDecimal("500000.00"), "USD");
        terminated.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.saveAndFlush(terminated);

        AnalyticsSummaryResponse summary = analyticsRepository.summary();
        assertThat(summary.headcount()).isZero();
    }

    private Employee hireEmployee(
            String email, com.acme.salary.entity.Department department, Country country,
            BigDecimal amount, String currencyCode) {
        Employee employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-" + email.hashCode())
                .firstName("Test")
                .lastName("Employee")
                .email(email)
                .department(department)
                .country(country)
                .jobTitle("Engineer")
                .hireDate(LocalDate.of(2023, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build());
        compensationRecordRepository.save(CompensationRecord.builder()
                .employee(employee)
                .amount(amount)
                .currencyCode(currencyCode)
                .effectiveDate(LocalDate.of(2023, 1, 1))
                .reason(CompensationReason.HIRE)
                .build());
        return employee;
    }
}
