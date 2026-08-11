# AI Usage

This project was built with Claude Code (Sonnet 5) as a pair-programmer across the full stack -
planning, implementation, test-writing, debugging, and browser verification. This doc describes
how, not as a transcript, but as the decisions that mattered.

## Requirements clarification

Before any code, I asked Claude to gather open requirements rather than assume them. It used the
assessment brief plus the assessor's clarification email (that "answer questions about pay" means
BI/dashboards, not a chatbot) as context, then asked me to decide the genuinely ambiguous points
via structured questions: backend language (Java/Spring Boot, per the target JD), frontend stack
(React/Vite/JavaScript + MUI, not TypeScript/Next.js), how to model multi-currency salary (native
currency + FX conversion at read time vs. a single base currency), whether to track salary history
or just current salary, and whether to build auth. Those answers became locked decisions in
`docs/requirements.md` and the architecture plan before implementation started - I didn't want the
model guessing on product-shape questions that were mine to make.

## Planning before code

Claude used its plan mode to draft a full build plan - repo structure, data model, API surface,
commit sequence - before touching the filesystem, and asked for two rounds of correction
(JavaScript instead of TypeScript on the frontend; explicit SOLID/design-pattern reasoning on the
backend, since I wanted to see deliberate architecture, not just working code). Both were folded
into the plan and shape the codebase you're reading (see `docs/architecture.md` for what that
produced concretely - split services, DTOs at the API boundary, Specifications for filtering).

## Build loop: implement, verify, fix

Each feature area (data model, employee CRUD, compensation history, seed script, analytics,
frontend) was its own commit, and each was verified before moving on rather than assumed correct:

- **Toolchain friction was real and had to be debugged, not papered over.** This project landed on
  very new versions (Spring Boot 4.1, Java 25 available locally, Spring Data JPA's restructured
  test-autoconfigure packages). Lombok's annotation processor wasn't being picked up by the default
  Maven compiler plugin config, `@DataJpaTest`/`AutoConfigureTestDatabase` had moved to new package
  namespaces, and `Specification.where(null)` was ambiguous between two overloads in the newer
  Spring Data API. Each of these surfaced as a compile or test failure and was fixed by reading the
  actual error and inspecting the installed jars (e.g. `unzip -l` on the Spring Boot test jars to
  find where `DataJpaTest.class` actually lived), not by downgrading dependencies to something
  more familiar.
- **A repository-level test caught a real bug.** The analytics currency conversion was initially
  inverted - dividing by `rate_to_usd` instead of multiplying - which produced a total payroll
  figure off by roughly four orders of magnitude. This wasn't caught by the code compiling or the
  happy-path smoke test; it was caught by a repository test with a small, hand-computed fixture
  (3 employees, exact expected USD totals), which is the point of writing that kind of test instead
  of only asserting "the endpoint returns 200."
- **The seed script was run for real, twice.** Once at a small scale (300 employees) to check
  correctness quickly, once at full scale (10,000) to confirm timing (~3.5s) and that the dataset
  stayed deterministic across runs with the same random seed - not just read as plausible-looking
  code.
- **The frontend was verified in an actual browser, not just built.** `npm run build` succeeding
  doesn't prove the UI works. Claude installed Playwright, ran the Vite dev server against the
  seeded backend, navigated the dashboard/employee-list/employee-detail flow, and took screenshots
  - which surfaced two React DOM prop warnings (`alignItems`/`justifyContent` passed as direct
  component props instead of via `sx`) that neither the build nor the test suite would have caught.
  It also caught that the default Vite locale (`en-IN` in this sandbox) broke currency-formatting
  test assertions written against US formatting conventions - fixed by pinning the app's
  `Intl.NumberFormat` locale explicitly, which is also better UX (consistent formatting for every
  HR Manager regardless of their OS locale) than the bug it fixed.

## What I did not delegate

Product-shape decisions (scope, what to leave out and why, tech stack, data model trade-offs like
compensation-as-history vs. a single field) were mine, made explicitly via the clarifying
questions above rather than left to the model's judgment. Claude's job was turning those decisions
into working, tested code, and pushing back on my request that architecture reasoning be explicit
(the SOLID/design-pattern feedback above) rather than silently agreeing.
