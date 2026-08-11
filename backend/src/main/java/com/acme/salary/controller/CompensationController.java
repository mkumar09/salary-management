package com.acme.salary.controller;

import com.acme.salary.dto.CompensationRecordCreateRequest;
import com.acme.salary.dto.CompensationRecordResponse;
import com.acme.salary.service.CompensationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}/compensation")
public class CompensationController {

    private final CompensationService compensationService;

    public CompensationController(CompensationService compensationService) {
        this.compensationService = compensationService;
    }

    @GetMapping
    public List<CompensationRecordResponse> history(@PathVariable Long employeeId) {
        return compensationService.getHistory(employeeId);
    }

    @PostMapping
    public ResponseEntity<CompensationRecordResponse> addRecord(
            @PathVariable Long employeeId, @Valid @RequestBody CompensationRecordCreateRequest request) {
        CompensationRecordResponse created = compensationService.addRecord(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
