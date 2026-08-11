# Requirements — Salary Management System

## Goal
Give ACME's HR Manager a web-based system to manage salary data for ~10,000 employees across
multiple countries, replacing spreadsheets, and to answer questions about how the org pays people
through structured dashboards rather than manual pivot tables.

## User
HR Manager (single persona, single tenant).

## Scope & Features

**Employee & compensation management (CRUD)**
- Maintain employee records: name, email, job title, department, country, hire date, employment status.
- Maintain compensation as a *history*, not a single field — each employee has one or more
  compensation records (hire, raise, adjustment) with an amount, currency, and effective date.
  "Current salary" is derived as the latest record effective on or before today. This is the one
  non-obvious data modeling decision in the system: it costs a join but buys raise history and
  "as of" queries for free, which a spreadsheet-replacement tool should have.
- Search, filter (by department/country/status), and paginate the employee list — required at
  10,000 rows, since loading everything client-side isn't viable.

**Compensation intelligence (the "answer questions about pay" requirement)**
- Per the assessment owner's clarification, this means structured BI, not a chatbot: a dashboard
  showing total headcount, total payroll, average/median salary, and breakdowns by department and
  by country, plus a salary distribution view.
- Multiple countries implies multiple currencies. Salaries are stored in their native currency and
  converted to a common base currency (USD) for cross-country aggregates, using a seeded static
  exchange-rate table.

**Seed data**
- A deterministic seed script generates 10,000 employees spread across ~6 countries/currencies and
  ~7 departments, with realistic salary bands and 1–3 compensation records each, so the dashboards
  and list views are meaningful out of the box.

## Deliberately Out of Scope (and why)

- **AI chatbot / natural-language querying** — explicitly deprioritized by the assessment owner in
  favor of core CRUD, data architecture, and analytics. Not built.
- **Authentication / multi-user roles** — the stated persona is a single HR Manager with no
  multi-tenant or permissions requirement. Building auth would be speculative scope; it's a clean
  seam to add later (the API is already stateless) but isn't needed to answer the problem as stated.
- **Historical / time-series exchange rates** — FX rates are seeded as a static table (one rate per
  currency), not date-versioned. Real payroll systems care about this, but it adds a second axis of
  "as of" complexity on top of compensation history for a scenario that doesn't call for it.
- **Payroll processing / tax / benefits / deductions** — this is a *salary record and reporting*
  tool, not a payroll run engine. Computing net pay, tax withholding, or benefits is a materially
  different (and much larger) problem than the one described.
- **Org chart / manager hierarchy** — not mentioned in the requirements and not needed to answer
  compensation questions at the department/country level asked for.
- **Bulk import/export (CSV/Excel upload)** — genuinely useful for an HR tool migrating off Excel,
  but not required by the brief; the seed script covers the "getting data in" need for this exercise.
  Flagged here as the most likely next feature if this were a real product.
