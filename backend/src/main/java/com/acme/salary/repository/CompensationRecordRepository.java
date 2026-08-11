package com.acme.salary.repository;

import com.acme.salary.entity.CompensationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRecordRepository extends JpaRepository<CompensationRecord, Long> {

    List<CompensationRecord> findByEmployeeIdOrderByEffectiveDateDescIdDesc(Long employeeId);
}
