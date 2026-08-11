import { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import Alert from "@mui/material/Alert";

const STATUS_OPTIONS = ["ACTIVE", "TERMINATED"];

/**
 * Shared create/edit form. `mode="create"` shows the initial-salary fields (a new employee always
 * gets a HIRE compensation record); `mode="edit"` shows employment status instead - salary changes
 * after hire go through the compensation history endpoint, not this form.
 */
export default function EmployeeForm({ mode, initialValues, departments, countries, submitting, error, onSubmit }) {
  const [values, setValues] = useState(initialValues);

  useEffect(() => {
    setValues(initialValues);
  }, [initialValues]);

  function set(field, value) {
    setValues((prev) => ({ ...prev, [field]: value }));
  }

  function handleCountryChange(countryId) {
    const country = countries.find((c) => String(c.id) === String(countryId));
    setValues((prev) => ({
      ...prev,
      countryId,
      ...(mode === "create" && country ? { initialSalaryCurrency: country.currencyCode } : {}),
    }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit(values);
  }

  return (
    <form onSubmit={handleSubmit}>
      <Grid container spacing={2}>
        {error && (
          <Grid size={12}>
            <Alert severity="error">{error}</Alert>
          </Grid>
        )}
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="First name"
            fullWidth
            required
            value={values.firstName}
            onChange={(e) => set("firstName", e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="Last name"
            fullWidth
            required
            value={values.lastName}
            onChange={(e) => set("lastName", e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="Email"
            type="email"
            fullWidth
            required
            value={values.email}
            onChange={(e) => set("email", e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="Job title"
            fullWidth
            required
            value={values.jobTitle}
            onChange={(e) => set("jobTitle", e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            select
            label="Department"
            fullWidth
            required
            value={values.departmentId}
            onChange={(e) => set("departmentId", e.target.value)}
          >
            {departments.map((d) => (
              <MenuItem key={d.id} value={d.id}>
                {d.name}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            select
            label="Country"
            fullWidth
            required
            value={values.countryId}
            onChange={(e) => handleCountryChange(e.target.value)}
          >
            {countries.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.name}
              </MenuItem>
            ))}
          </TextField>
        </Grid>

        {mode === "create" && (
          <>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Hire date"
                type="date"
                fullWidth
                required
                slotProps={{ inputLabel: { shrink: true } }}
                value={values.hireDate}
                onChange={(e) => set("hireDate", e.target.value)}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField
                label="Initial salary"
                type="number"
                fullWidth
                required
                value={values.initialSalaryAmount}
                onChange={(e) => set("initialSalaryAmount", e.target.value)}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField
                label="Currency"
                fullWidth
                required
                value={values.initialSalaryCurrency}
                onChange={(e) => set("initialSalaryCurrency", e.target.value.toUpperCase())}
                slotProps={{ htmlInput: { maxLength: 3 } }}
              />
            </Grid>
          </>
        )}

        {mode === "edit" && (
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              select
              label="Employment status"
              fullWidth
              required
              value={values.employmentStatus}
              onChange={(e) => set("employmentStatus", e.target.value)}
            >
              {STATUS_OPTIONS.map((s) => (
                <MenuItem key={s} value={s}>
                  {s}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        )}
      </Grid>

      <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
        <Button type="submit" variant="contained" disabled={submitting}>
          {mode === "create" ? "Create employee" : "Save changes"}
        </Button>
      </Stack>
    </form>
  );
}
