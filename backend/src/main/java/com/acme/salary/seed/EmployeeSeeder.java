package com.acme.salary.seed;

import com.acme.salary.entity.CompensationReason;
import com.acme.salary.entity.CompensationRecord;
import com.acme.salary.entity.Country;
import com.acme.salary.entity.Department;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.entity.ExchangeRate;
import com.acme.salary.repository.CompensationRecordRepository;
import com.acme.salary.repository.CountryRepository;
import com.acme.salary.repository.DepartmentRepository;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Generates a deterministic 10,000-employee dataset on startup (guarded by app.seed.enabled and
 * only runs against an empty employee table, so it's safe to leave enabled across restarts).
 * Random but department/country reference data, salary bands, and job title progressions come
 * from SeedData so the distribution looks like a real org rather than pure noise.
 */
@Component
public class EmployeeSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSeeder.class);
    private static final int CHUNK_SIZE = 200;
    private static final int SENIORITY_LEVELS = 5;
    private static final int[] SENIORITY_WEIGHTS = {30, 30, 20, 12, 8};

    private final EmployeeRepository employeeRepository;
    private final CompensationRecordRepository compensationRecordRepository;
    private final DepartmentRepository departmentRepository;
    private final CountryRepository countryRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed.employee-count:10000}")
    private int employeeCount;

    @Value("${app.seed.random-seed:42}")
    private long randomSeed;

    public EmployeeSeeder(
            EmployeeRepository employeeRepository,
            CompensationRecordRepository compensationRecordRepository,
            DepartmentRepository departmentRepository,
            CountryRepository countryRepository,
            ExchangeRateRepository exchangeRateRepository) {
        this.employeeRepository = employeeRepository;
        this.compensationRecordRepository = compensationRecordRepository;
        this.departmentRepository = departmentRepository;
        this.countryRepository = countryRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (employeeRepository.count() > 0) {
            log.info("Seed skipped: employee table already has data");
            return;
        }

        List<Department> departments = departmentRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        Map<String, BigDecimal> rateToUsdByCurrency = exchangeRateRepository.findAll().stream()
                .collect(Collectors.toMap(ExchangeRate::getCurrencyCode, ExchangeRate::getRateToUsd));

        if (departments.isEmpty() || countries.isEmpty()) {
            log.warn("Seed skipped: reference data (departments/countries) not found - did migrations run?");
            return;
        }

        log.info("Seeding {} employees (random seed {})...", employeeCount, randomSeed);
        long startedAt = System.currentTimeMillis();
        Random random = new Random(randomSeed);
        LocalDate today = LocalDate.now();

        int created = 0;
        while (created < employeeCount) {
            int chunkTarget = Math.min(CHUNK_SIZE, employeeCount - created);
            List<Employee> chunk = new ArrayList<>(chunkTarget);
            List<BigDecimal[]> hireAmountAndFinalAmount = new ArrayList<>(chunkTarget);

            for (int i = 0; i < chunkTarget; i++) {
                created++;
                Department department = departments.get(random.nextInt(departments.size()));
                Country country = countries.get(random.nextInt(countries.size()));
                int seniority = weightedSeniority(random);

                String firstName = pick(SeedData.FIRST_NAMES, random);
                String lastName = pick(SeedData.LAST_NAMES, random);
                String email = "%s.%s.%d@acme.example".formatted(
                        firstName.toLowerCase(), lastName.toLowerCase().replace("'", ""), created);
                String jobTitle = SeedData.JOB_TITLES_BY_DEPARTMENT.get(department.getName()).get(seniority);
                LocalDate hireDate = today.minusDays(30L + random.nextInt(10 * 365));

                BigDecimal rateToUsd = rateToUsdByCurrency.getOrDefault(country.getCurrencyCode(), BigDecimal.ONE);
                BigDecimal finalAmount = targetSalary(department.getName(), seniority, rateToUsd, random);

                Employee employee = Employee.builder()
                        .employeeCode(nextEmployeeCode())
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .department(department)
                        .country(country)
                        .jobTitle(jobTitle)
                        .hireDate(hireDate)
                        .employmentStatus(EmploymentStatus.ACTIVE)
                        .build();
                chunk.add(employee);
                hireAmountAndFinalAmount.add(new BigDecimal[] {finalAmount});
            }

            List<Employee> saved = employeeRepository.saveAll(chunk);
            List<CompensationRecord> compensationRecords = new ArrayList<>();
            for (int i = 0; i < saved.size(); i++) {
                Employee employee = saved.get(i);
                BigDecimal finalAmount = hireAmountAndFinalAmount.get(i)[0];
                compensationRecords.addAll(
                        buildCompensationHistory(employee, finalAmount, today, random));
            }
            compensationRecordRepository.saveAll(compensationRecords);

            if (created % 2000 == 0 || created == employeeCount) {
                log.info("Seeded {}/{} employees", created, employeeCount);
            }
        }

        log.info("Seed complete: {} employees in {} ms", employeeCount, System.currentTimeMillis() - startedAt);
    }

    int weightedSeniority(Random random) {
        int roll = random.nextInt(100);
        int cumulative = 0;
        for (int level = 0; level < SENIORITY_LEVELS; level++) {
            cumulative += SENIORITY_WEIGHTS[level];
            if (roll < cumulative) {
                return level;
            }
        }
        return SENIORITY_LEVELS - 1;
    }

    BigDecimal targetSalary(String departmentName, int seniority, BigDecimal rateToUsd, Random random) {
        int[] range = SeedData.SALARY_RANGE_USD_BY_DEPARTMENT.get(departmentName);
        double fraction = seniority / (double) (SENIORITY_LEVELS - 1);
        double base = range[0] + (range[1] - range[0]) * fraction;
        double variance = 0.9 + random.nextDouble() * 0.2; // +/-10%
        BigDecimal amountUsd = BigDecimal.valueOf(base * variance).setScale(2, RoundingMode.HALF_UP);
        return amountUsd.divide(rateToUsd, 2, RoundingMode.HALF_UP);
    }

    List<CompensationRecord> buildCompensationHistory(
            Employee employee, BigDecimal finalAmount, LocalDate today, Random random) {
        long tenureDays = java.time.temporal.ChronoUnit.DAYS.between(employee.getHireDate(), today);
        int maxRaises = tenureDays >= 3 * 365 ? 2 : (tenureDays >= 365 ? 1 : 0);
        int numRaises = maxRaises == 0 ? 0 : random.nextInt(maxRaises + 1);

        List<CompensationRecord> records = new ArrayList<>();
        BigDecimal raiseFactor = BigDecimal.valueOf(1.10);
        BigDecimal hireAmount = finalAmount.divide(raiseFactor.pow(numRaises), 2, RoundingMode.HALF_UP);

        records.add(CompensationRecord.builder()
                .employee(employee)
                .amount(hireAmount)
                .currencyCode(employee.getCountry().getCurrencyCode())
                .effectiveDate(employee.getHireDate())
                .reason(CompensationReason.HIRE)
                .build());

        BigDecimal runningAmount = hireAmount;
        for (int raiseIndex = 1; raiseIndex <= numRaises; raiseIndex++) {
            long dayOffset = tenureDays * raiseIndex / (numRaises + 1);
            LocalDate effectiveDate = employee.getHireDate().plusDays(Math.max(dayOffset, 30));
            runningAmount = raiseIndex == numRaises
                    ? finalAmount
                    : runningAmount.multiply(raiseFactor).setScale(2, RoundingMode.HALF_UP);
            records.add(CompensationRecord.builder()
                    .employee(employee)
                    .amount(runningAmount)
                    .currencyCode(employee.getCountry().getCurrencyCode())
                    .effectiveDate(effectiveDate)
                    .reason(CompensationReason.RAISE)
                    .build());
        }
        return records;
    }

    private String pick(List<String> values, Random random) {
        return values.get(random.nextInt(values.size()));
    }

    private String nextEmployeeCode() {
        return "EMP-%06d".formatted(employeeRepository.nextEmployeeCodeSequenceValue());
    }
}
