package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Composable filters for the paginated employee list - avoids one query method per filter combination. */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> search(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) {
            return null;
        }
        String pattern = "%" + searchTerm.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("employeeCode")), pattern));
    }

    public static Specification<Employee> hasDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<Employee> hasCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("country").get("id"), countryId);
    }

    public static Specification<Employee> hasStatus(EmploymentStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("employmentStatus"), status);
    }

    public static Specification<Employee> combine(
            String searchTerm, Long departmentId, Long countryId, EmploymentStatus status) {
        List<Specification<Employee>> filters = new ArrayList<>();
        for (Specification<Employee> filter : Arrays.asList(
                search(searchTerm), hasDepartment(departmentId), hasCountry(countryId), hasStatus(status))) {
            if (filter != null) {
                filters.add(filter);
            }
        }
        return Specification.allOf(filters);
    }
}
