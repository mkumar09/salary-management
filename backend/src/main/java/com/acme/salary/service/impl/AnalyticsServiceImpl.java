package com.acme.salary.service.impl;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.CountryBreakdown;
import com.acme.salary.dto.DepartmentBreakdown;
import com.acme.salary.dto.SalaryDistributionBucket;
import com.acme.salary.repository.AnalyticsRepository;
import com.acme.salary.service.AnalyticsService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary() {
        return analyticsRepository.summary();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentBreakdown> byDepartment() {
        return analyticsRepository.byDepartment();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryBreakdown> byCountry() {
        return analyticsRepository.byCountry();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryDistributionBucket> distribution() {
        return analyticsRepository.distribution();
    }
}
