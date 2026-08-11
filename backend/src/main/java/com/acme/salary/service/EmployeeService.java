package com.acme.salary.service;

import com.acme.salary.dto.EmployeeCreateRequest;
import com.acme.salary.dto.EmployeeResponse;
import com.acme.salary.dto.EmployeeUpdateRequest;
import com.acme.salary.dto.PageResponse;
import com.acme.salary.entity.EmploymentStatus;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    PageResponse<EmployeeResponse> listEmployees(
            String search, Long departmentId, Long countryId, EmploymentStatus status, Pageable pageable);

    EmployeeResponse getEmployee(Long id);

    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);

    void deleteEmployee(Long id);
}
