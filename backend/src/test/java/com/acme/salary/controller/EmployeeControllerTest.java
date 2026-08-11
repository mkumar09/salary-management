package com.acme.salary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.dto.CountryResponse;
import com.acme.salary.dto.DepartmentResponse;
import com.acme.salary.dto.EmployeeResponse;
import com.acme.salary.dto.PageResponse;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.exception.NotFoundException;
import com.acme.salary.service.EmployeeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private static EmployeeResponse sampleResponse() {
        return new EmployeeResponse(
                1L, "EMP-000001", "Ada", "Lovelace", "ada@example.com",
                new DepartmentResponse(1L, "Engineering"),
                new CountryResponse(1L, "United States", "US", "USD"),
                "Software Engineer", LocalDate.of(2024, 1, 10), EmploymentStatus.ACTIVE,
                new BigDecimal("100000.00"), "USD",
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void list_returnsPagedEmployees() throws Exception {
        Page<EmployeeResponse> page = new PageImpl<>(java.util.List.of(sampleResponse()), PageRequest.of(0, 25), 1);
        when(employeeService.listEmployees(any(), any(), any(), any(), any()))
                .thenReturn(PageResponse.of(page));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP-000001"));
    }

    @Test
    void get_returns404WhenMissing() throws Exception {
        when(employeeService.getEmployee(99L)).thenThrow(new NotFoundException("Employee not found: 99"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_returns400OnInvalidPayload() throws Exception {
        String invalidJson = """
                {"firstName": "", "lastName": "Lovelace", "email": "not-an-email"}
                """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void delete_returns204OnSuccess() throws Exception {
        mockMvc.perform(delete("/api/employees/1")).andExpect(status().isNoContent());
    }
}
