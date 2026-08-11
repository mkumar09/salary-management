package com.acme.salary.service.impl;

import com.acme.salary.dto.CountryResponse;
import com.acme.salary.dto.DepartmentResponse;
import com.acme.salary.dto.EmployeeCreateRequest;
import com.acme.salary.dto.EmployeeResponse;
import com.acme.salary.dto.EmployeeUpdateRequest;
import com.acme.salary.dto.PageResponse;
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
import com.acme.salary.repository.EmployeeSpecifications;
import com.acme.salary.service.CompensationService;
import com.acme.salary.service.EmployeeService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CountryRepository countryRepository;
    private final CurrentCompensationRepository currentCompensationRepository;
    private final CompensationService compensationService;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            CountryRepository countryRepository,
            CurrentCompensationRepository currentCompensationRepository,
            CompensationService compensationService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.countryRepository = countryRepository;
        this.currentCompensationRepository = currentCompensationRepository;
        this.compensationService = compensationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> listEmployees(
            String search, Long departmentId, Long countryId, EmploymentStatus status, Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(
                EmployeeSpecifications.combine(search, departmentId, countryId, status), pageable);

        List<Long> employeeIds = page.getContent().stream().map(Employee::getId).toList();
        Map<Long, CurrentCompensation> currentByEmployeeId = currentCompensationRepository
                .findAllById(employeeIds).stream()
                .collect(java.util.stream.Collectors.toMap(CurrentCompensation::getEmployeeId, Function.identity()));

        return PageResponse.of(page.map(employee -> toResponse(employee, currentByEmployeeId.get(employee.getId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        CurrentCompensation current = currentCompensationRepository.findByEmployeeId(id).orElse(null);
        return toResponse(employee, current);
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An employee with email " + request.email() + " already exists");
        }
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + request.departmentId()));
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new NotFoundException("Country not found: " + request.countryId()));

        String employeeCode = generateEmployeeCode();
        Employee employee = employeeRepository.save(Employee.builder()
                .employeeCode(employeeCode)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .department(department)
                .country(country)
                .jobTitle(request.jobTitle())
                .hireDate(request.hireDate())
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build());

        compensationService.recordHire(employee, request.initialSalaryAmount(), request.initialSalaryCurrency());

        CurrentCompensation current = currentCompensationRepository.findByEmployeeId(employee.getId()).orElse(null);
        return toResponse(employee, current);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        if (!employee.getEmail().equalsIgnoreCase(request.email())
                && employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("An employee with email " + request.email() + " already exists");
        }
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + request.departmentId()));
        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> new NotFoundException("Country not found: " + request.countryId()));

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setDepartment(department);
        employee.setCountry(country);
        employee.setJobTitle(request.jobTitle());
        employee.setEmploymentStatus(request.employmentStatus());

        CurrentCompensation current = currentCompensationRepository.findByEmployeeId(id).orElse(null);
        return toResponse(employee, current);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new NotFoundException("Employee not found: " + id);
        }
        employeeRepository.deleteById(id);
    }

    private String generateEmployeeCode() {
        long sequenceValue = employeeRepository.nextEmployeeCodeSequenceValue();
        return "EMP-%06d".formatted(sequenceValue);
    }

    private EmployeeResponse toResponse(Employee employee, CurrentCompensation current) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                new DepartmentResponse(employee.getDepartment().getId(), employee.getDepartment().getName()),
                new CountryResponse(
                        employee.getCountry().getId(),
                        employee.getCountry().getName(),
                        employee.getCountry().getIsoCode(),
                        employee.getCountry().getCurrencyCode()),
                employee.getJobTitle(),
                employee.getHireDate(),
                employee.getEmploymentStatus(),
                current != null ? current.getAmount() : null,
                current != null ? current.getCurrencyCode() : null,
                employee.getCreatedAt(),
                employee.getUpdatedAt());
    }
}
