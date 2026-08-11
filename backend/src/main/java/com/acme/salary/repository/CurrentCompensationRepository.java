package com.acme.salary.repository;

import com.acme.salary.entity.CurrentCompensation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentCompensationRepository extends JpaRepository<CurrentCompensation, Long> {

    Optional<CurrentCompensation> findByEmployeeId(Long employeeId);
}
