package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.CompensationRecord;
import com.acme.salary.entity.Country;
import com.acme.salary.entity.Department;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.repository.CompensationRecordRepository;
import com.acme.salary.repository.CountryRepository;
import com.acme.salary.repository.DepartmentRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Exercises the seed generator's pure logic directly - no Spring context, no database. */
class EmployeeSeederTest {

    private final EmployeeSeeder seeder = new EmployeeSeeder(
            mock(EmployeeRepository.class),
            mock(CompensationRecordRepository.class),
            mock(DepartmentRepository.class),
            mock(CountryRepository.class),
            mock(ExchangeRateRepository.class));

    @Test
    void weightedSeniority_alwaysReturnsValidLevel() {
        Random random = new Random(1);
        for (int i = 0; i < 1000; i++) {
            int level = seeder.weightedSeniority(random);
            assertThat(level).isBetween(0, 4);
        }
    }

    @Test
    void targetSalary_increasesWithSeniority() {
        Random random = new Random(7);
        BigDecimal junior = seeder.targetSalary("Engineering", 0, BigDecimal.ONE, random);
        BigDecimal senior = seeder.targetSalary("Engineering", 4, BigDecimal.ONE, random);

        assertThat(senior).isGreaterThan(junior);
    }

    @Test
    void targetSalary_convertsUsingExchangeRate() {
        Random random = new Random(7);
        BigDecimal rateToUsd = new BigDecimal("0.5"); // 1 local unit = 0.5 USD
        BigDecimal amountLocal = seeder.targetSalary("Engineering", 2, rateToUsd, new Random(7));
        BigDecimal amountUsdEquivalent = seeder.targetSalary("Engineering", 2, BigDecimal.ONE, new Random(7));

        // Same random draw, different rate -> local amount should be double the USD-equivalent amount.
        assertThat(amountLocal).isEqualByComparingTo(amountUsdEquivalent.multiply(new BigDecimal("2")));
    }

    @Test
    void buildCompensationHistory_startsWithHireAndEndsAtTargetAmount() {
        Employee employee = Employee.builder()
                .employeeCode("EMP-000001")
                .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                .department(Department.builder().id(1L).name("Engineering").build())
                .country(Country.builder().id(1L).name("US").isoCode("US").currencyCode("USD").build())
                .jobTitle("Engineer")
                .hireDate(LocalDate.now().minusYears(4))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
        BigDecimal finalAmount = new BigDecimal("150000.00");

        List<CompensationRecord> history = seeder.buildCompensationHistory(
                employee, finalAmount, LocalDate.now(), new Random(3));

        assertThat(history).isNotEmpty();
        assertThat(history.get(0).getReason()).isEqualTo(CompensationReason.HIRE);
        assertThat(history.get(history.size() - 1).getAmount()).isEqualByComparingTo(finalAmount);
        // Effective dates must be strictly increasing and never in the future.
        for (int i = 1; i < history.size(); i++) {
            assertThat(history.get(i).getEffectiveDate()).isAfter(history.get(i - 1).getEffectiveDate());
        }
        assertThat(history.get(history.size() - 1).getEffectiveDate()).isBeforeOrEqualTo(LocalDate.now());
    }

    @Test
    void buildCompensationHistory_newHireHasOnlyHireRecord() {
        Employee employee = Employee.builder()
                .employeeCode("EMP-000002")
                .firstName("Grace").lastName("Hopper").email("grace@example.com")
                .department(Department.builder().id(1L).name("Engineering").build())
                .country(Country.builder().id(1L).name("US").isoCode("US").currencyCode("USD").build())
                .jobTitle("Engineer")
                .hireDate(LocalDate.now().minusDays(10))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();

        List<CompensationRecord> history = seeder.buildCompensationHistory(
                employee, new BigDecimal("90000.00"), LocalDate.now(), new Random(3));

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getReason()).isEqualTo(CompensationReason.HIRE);
    }
}
