import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import { createEmployee } from "../api/employees";
import useLookups from "../hooks/useLookups";
import EmployeeForm from "../components/employees/EmployeeForm";

const EMPTY_VALUES = {
  firstName: "",
  lastName: "",
  email: "",
  departmentId: "",
  countryId: "",
  jobTitle: "",
  hireDate: new Date().toISOString().slice(0, 10),
  initialSalaryAmount: "",
  initialSalaryCurrency: "USD",
};

export default function NewEmployeePage() {
  const navigate = useNavigate();
  const { departments, countries, loading } = useLookups();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  async function handleSubmit(values) {
    setSubmitting(true);
    setError(null);
    try {
      const created = await createEmployee({
        ...values,
        departmentId: Number(values.departmentId),
        countryId: Number(values.countryId),
        initialSalaryAmount: Number(values.initialSalaryAmount),
      });
      navigate(`/employees/${created.id}`);
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to create employee");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return null;

  return (
    <Paper sx={{ p: 3, maxWidth: 800 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Add Employee
      </Typography>
      <EmployeeForm
        mode="create"
        initialValues={EMPTY_VALUES}
        departments={departments}
        countries={countries}
        submitting={submitting}
        error={error}
        onSubmit={handleSubmit}
      />
    </Paper>
  );
}
