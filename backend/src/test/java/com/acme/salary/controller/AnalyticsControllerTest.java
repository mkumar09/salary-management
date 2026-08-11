package com.acme.salary.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.service.AnalyticsService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void summary_returnsPayrollFigures() throws Exception {
        when(analyticsService.summary()).thenReturn(new AnalyticsSummaryResponse(
                10000L, new BigDecimal("850000000.00"), new BigDecimal("85000.00"), new BigDecimal("78000.00")));

        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headcount").value(10000))
                .andExpect(jsonPath("$.medianSalaryUsd").value(78000.00));
    }
}
