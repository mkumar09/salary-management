-- Reference/lookup data the app depends on (which countries & departments exist, current FX
-- rates). This is deterministic domain data, not random test fixtures, so it lives in a
-- migration rather than the seed script - the seed script only generates the 10,000 employees.

INSERT INTO country (name, iso_code, currency_code) VALUES
    ('United States', 'US', 'USD'),
    ('United Kingdom', 'GB', 'GBP'),
    ('Germany', 'DE', 'EUR'),
    ('India', 'IN', 'INR'),
    ('Canada', 'CA', 'CAD'),
    ('Australia', 'AU', 'AUD');

INSERT INTO department (name) VALUES
    ('Engineering'),
    ('Sales'),
    ('Marketing'),
    ('Finance'),
    ('Human Resources'),
    ('Customer Support'),
    ('Product');

-- Static rates for reporting/aggregation only (see docs/requirements.md: historical/time-series
-- FX is explicitly out of scope). Approximate, not tied to a live feed.
INSERT INTO exchange_rate (currency_code, rate_to_usd) VALUES
    ('USD', 1.00000000),
    ('GBP', 1.27000000),
    ('EUR', 1.08000000),
    ('INR', 0.01200000),
    ('CAD', 0.73000000),
    ('AUD', 0.66000000);
