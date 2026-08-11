-- "Current salary" is derived, not stored: the latest compensation_record per employee by
-- effective_date. Centralizing that as a view means every analytics/reporting query reads the
-- same definition of "current" instead of each query re-deriving it (and risking drift).
CREATE VIEW current_compensation AS
SELECT
    ranked.employee_id,
    ranked.amount,
    ranked.currency_code,
    ranked.effective_date,
    ranked.reason
FROM (
    SELECT
        cr.employee_id,
        cr.amount,
        cr.currency_code,
        cr.effective_date,
        cr.reason,
        ROW_NUMBER() OVER (
            PARTITION BY cr.employee_id
            ORDER BY cr.effective_date DESC, cr.id DESC
        ) AS rn
    FROM compensation_record cr
) ranked
WHERE ranked.rn = 1;
