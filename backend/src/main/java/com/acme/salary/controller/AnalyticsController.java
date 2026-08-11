package com.acme.salary.controller;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.CountryBreakdown;
import com.acme.salary.dto.DepartmentBreakdown;
import com.acme.salary.dto.SalaryDistributionBucket;
import com.acme.salary.service.AnalyticsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse summary() {
        return analyticsService.summary();
    }

    @GetMapping("/by-department")
    public List<DepartmentBreakdown> byDepartment() {
        return analyticsService.byDepartment();
    }

    @GetMapping("/by-country")
    public List<CountryBreakdown> byCountry() {
        return analyticsService.byCountry();
    }

    @GetMapping("/distribution")
    public List<SalaryDistributionBucket> distribution() {
        return analyticsService.distribution();
    }
}
