package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeCode(String employeeCode);

    @Query(value = "SELECT nextval('employee_code_seq')", nativeQuery = true)
    long nextEmployeeCodeSequenceValue();
}
