-- Business identifiers (employee_code) are sequence-backed rather than derived from the numeric
-- primary key, so they stay stable even if the PK strategy ever changes.
CREATE SEQUENCE employee_code_seq START WITH 1 INCREMENT BY 1;
