package com.acme.salary.seed;

import java.util.List;
import java.util.Map;

/** Static reference lists the seed generator draws from - kept separate from the generation logic itself. */
final class SeedData {

    private SeedData() {
    }

    static final List<String> FIRST_NAMES = List.of(
            "Aiden", "Amara", "Arjun", "Bianca", "Carlos", "Chen", "Daniel", "Deepa", "Elena", "Emeka",
            "Fatima", "Felix", "Grace", "Hana", "Ibrahim", "Isla", "Jack", "Jing", "Kai", "Kavya",
            "Liam", "Lucia", "Marco", "Maya", "Noah", "Nora", "Omar", "Priya", "Quinn", "Ravi",
            "Rosa", "Sam", "Sofia", "Tariq", "Uma", "Victor", "Wei", "Xavier", "Yuki", "Zara");

    static final List<String> LAST_NAMES = List.of(
            "Adams", "Bakshi", "Chen", "Costa", "Dubois", "Eriksson", "Fernandez", "Gupta", "Haddad", "Ibrahim",
            "Jensen", "Kumar", "Lopez", "Mueller", "Nakamura", "O'Brien", "Patel", "Quinn", "Rossi", "Silva",
            "Tanaka", "Ueda", "Vance", "Wagner", "Xu", "Yamamoto", "Zhang", "Anderson", "Brown", "Clarke",
            "Davies", "Evans", "Fischer", "Garcia", "Harris", "Iyer", "Johansson", "Kowalski", "Larsen", "Mehta");

    /** Job title progression within a department, roughly junior -> senior -> lead. */
    static final Map<String, List<String>> JOB_TITLES_BY_DEPARTMENT = Map.of(
            "Engineering", List.of("Software Engineer I", "Software Engineer II", "Senior Software Engineer",
                    "Staff Software Engineer", "Engineering Manager"),
            "Sales", List.of("Sales Development Rep", "Account Executive", "Senior Account Executive",
                    "Sales Manager", "Regional Sales Director"),
            "Marketing", List.of("Marketing Associate", "Marketing Specialist", "Senior Marketing Specialist",
                    "Marketing Manager", "Head of Marketing"),
            "Finance", List.of("Financial Analyst", "Senior Financial Analyst", "Finance Manager",
                    "Controller", "Finance Director"),
            "Human Resources", List.of("HR Coordinator", "HR Generalist", "Senior HR Generalist",
                    "HR Business Partner", "HR Director"),
            "Customer Support", List.of("Support Associate", "Support Specialist", "Senior Support Specialist",
                    "Support Team Lead", "Support Manager"),
            "Product", List.of("Associate Product Manager", "Product Manager", "Senior Product Manager",
                    "Group Product Manager", "Director of Product"));

    /** Annual base salary range in USD-equivalent, before country cost-of-living conversion. */
    static final Map<String, int[]> SALARY_RANGE_USD_BY_DEPARTMENT = Map.of(
            "Engineering", new int[] {75000, 190000},
            "Sales", new int[] {55000, 160000},
            "Marketing", new int[] {55000, 150000},
            "Finance", new int[] {60000, 170000},
            "Human Resources", new int[] {50000, 140000},
            "Customer Support", new int[] {40000, 100000},
            "Product", new int[] {80000, 195000});
}
