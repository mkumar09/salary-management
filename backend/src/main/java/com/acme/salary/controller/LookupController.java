package com.acme.salary.controller;

import com.acme.salary.dto.CountryResponse;
import com.acme.salary.dto.DepartmentResponse;
import com.acme.salary.repository.CountryRepository;
import com.acme.salary.repository.DepartmentRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only lookups for populating dropdowns - departments and countries are reference data. */
@RestController
@RequestMapping("/api")
public class LookupController {

    private final DepartmentRepository departmentRepository;
    private final CountryRepository countryRepository;

    public LookupController(DepartmentRepository departmentRepository, CountryRepository countryRepository) {
        this.departmentRepository = departmentRepository;
        this.countryRepository = countryRepository;
    }

    @GetMapping("/departments")
    public List<DepartmentResponse> departments() {
        return departmentRepository.findAll().stream()
                .map(d -> new DepartmentResponse(d.getId(), d.getName()))
                .toList();
    }

    @GetMapping("/countries")
    public List<CountryResponse> countries() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryResponse(c.getId(), c.getName(), c.getIsoCode(), c.getCurrencyCode()))
                .toList();
    }
}
