package com.acme.salary.repository;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.CountryBreakdown;
import com.acme.salary.dto.DepartmentBreakdown;
import com.acme.salary.dto.SalaryDistributionBucket;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Cross-cutting payroll aggregates. Deliberately plain JdbcTemplate + native SQL rather than
 * JPQL/Specifications - these queries join Employee, the current_compensation view, and
 * exchange_rate (a non-FK join on currency_code) and run entirely in the database, never pulling
 * per-employee rows into the app, which matters once this is 10k+ (and someday more) rows.
 *
 * Scope note: figures are computed over ACTIVE employees only - a terminated employee's last
 * salary isn't part of current payroll.
 */
@Repository
public class AnalyticsRepository {

    private static final String CURRENT_USD_COMPENSATION_CTE = """
            WITH current_usd_compensation AS (
                SELECT e.id AS employee_id, e.department_id, e.country_id,
                       cc.amount * er.rate_to_usd AS amount_usd
                FROM employee e
                JOIN current_compensation cc ON cc.employee_id = e.id
                JOIN exchange_rate er ON er.currency_code = cc.currency_code
                WHERE e.employment_status = 'ACTIVE'
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AnalyticsSummaryResponse summary() {
        String sql = CURRENT_USD_COMPENSATION_CTE + """
                SELECT
                    COUNT(*) AS headcount,
                    COALESCE(SUM(amount_usd), 0) AS total_payroll_usd,
                    COALESCE(AVG(amount_usd), 0) AS average_salary_usd,
                    COALESCE(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY amount_usd), 0) AS median_salary_usd
                FROM current_usd_compensation
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new AnalyticsSummaryResponse(
                rs.getLong("headcount"),
                rs.getBigDecimal("total_payroll_usd"),
                rs.getBigDecimal("average_salary_usd"),
                rs.getBigDecimal("median_salary_usd")));
    }

    public List<DepartmentBreakdown> byDepartment() {
        String sql = CURRENT_USD_COMPENSATION_CTE + """
                SELECT d.id AS department_id, d.name AS department_name,
                       COUNT(*) AS headcount,
                       SUM(c.amount_usd) AS total_payroll_usd,
                       AVG(c.amount_usd) AS average_salary_usd
                FROM current_usd_compensation c
                JOIN department d ON d.id = c.department_id
                GROUP BY d.id, d.name
                ORDER BY d.name
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DepartmentBreakdown(
                rs.getLong("department_id"),
                rs.getString("department_name"),
                rs.getLong("headcount"),
                rs.getBigDecimal("total_payroll_usd"),
                rs.getBigDecimal("average_salary_usd")));
    }

    public List<CountryBreakdown> byCountry() {
        String sql = CURRENT_USD_COMPENSATION_CTE + """
                SELECT co.id AS country_id, co.name AS country_name, co.currency_code,
                       COUNT(*) AS headcount,
                       SUM(c.amount_usd) AS total_payroll_usd,
                       AVG(c.amount_usd) AS average_salary_usd
                FROM current_usd_compensation c
                JOIN country co ON co.id = c.country_id
                GROUP BY co.id, co.name, co.currency_code
                ORDER BY co.name
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CountryBreakdown(
                rs.getLong("country_id"),
                rs.getString("country_name"),
                rs.getString("currency_code"),
                rs.getLong("headcount"),
                rs.getBigDecimal("total_payroll_usd"),
                rs.getBigDecimal("average_salary_usd")));
    }

    /** Fixed USD bands rather than dynamic quantiles - stable, comparable buckets across reloads. */
    public List<SalaryDistributionBucket> distribution() {
        String sql = CURRENT_USD_COMPENSATION_CTE + """
                , bucketed AS (
                    SELECT
                        CASE
                            WHEN amount_usd < 50000 THEN '< 50K'
                            WHEN amount_usd < 75000 THEN '50K-75K'
                            WHEN amount_usd < 100000 THEN '75K-100K'
                            WHEN amount_usd < 125000 THEN '100K-125K'
                            WHEN amount_usd < 150000 THEN '125K-150K'
                            WHEN amount_usd < 175000 THEN '150K-175K'
                            WHEN amount_usd < 200000 THEN '175K-200K'
                            ELSE '200K+'
                        END AS bucket_label,
                        CASE
                            WHEN amount_usd < 50000 THEN 0
                            WHEN amount_usd < 75000 THEN 1
                            WHEN amount_usd < 100000 THEN 2
                            WHEN amount_usd < 125000 THEN 3
                            WHEN amount_usd < 150000 THEN 4
                            WHEN amount_usd < 175000 THEN 5
                            WHEN amount_usd < 200000 THEN 6
                            ELSE 7
                        END AS bucket_order
                    FROM current_usd_compensation
                )
                SELECT bucket_label, COUNT(*) AS cnt
                FROM bucketed
                GROUP BY bucket_label, bucket_order
                ORDER BY bucket_order
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new SalaryDistributionBucket(rs.getString("bucket_label"), rs.getLong("cnt")));
    }
}
