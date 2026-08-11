import { useState } from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import EmployeeFilters from "./EmployeeFilters";

const DEPARTMENTS = [{ id: 1, name: "Engineering" }];
const COUNTRIES = [{ id: 1, name: "United States" }];
const EMPTY_FILTERS = { search: "", departmentId: "", countryId: "", status: "" };

/** Controlled inputs need a real state loop to behave like they would in the app. */
function StatefulFilters({ onChangeSpy }) {
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  return (
    <EmployeeFilters
      filters={filters}
      onChange={(next) => {
        setFilters(next);
        onChangeSpy(next);
      }}
      departments={DEPARTMENTS}
      countries={COUNTRIES}
    />
  );
}

function renderFilters() {
  const onChangeSpy = vi.fn();
  render(
    <MemoryRouter>
      <StatefulFilters onChangeSpy={onChangeSpy} />
    </MemoryRouter>,
  );
  return onChangeSpy;
}

describe("EmployeeFilters", () => {
  it("calls onChange with the accumulated search term, preserving other filters", async () => {
    const onChangeSpy = renderFilters();
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/search name, email, or code/i), "ada");

    expect(onChangeSpy).toHaveBeenLastCalledWith({
      search: "ada",
      departmentId: "",
      countryId: "",
      status: "",
    });
  });

  it("renders an Add Employee action", () => {
    renderFilters();
    expect(screen.getByRole("button", { name: /add employee/i })).toBeInTheDocument();
  });
});
