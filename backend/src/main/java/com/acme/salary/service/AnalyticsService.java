package com.acme.salary.service;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.CountryBreakdown;
import com.acme.salary.dto.DepartmentBreakdown;
import com.acme.salary.dto.SalaryDistributionBucket;
import java.util.List;

public interface AnalyticsService {

    AnalyticsSummaryResponse summary();

    List<DepartmentBreakdown> byDepartment();

    List<CountryBreakdown> byCountry();

    List<SalaryDistributionBucket> distribution();
}
