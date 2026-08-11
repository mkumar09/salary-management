import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import Divider from "@mui/material/Divider";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import CircularProgress from "@mui/material/CircularProgress";
import Box from "@mui/material/Box";
import { deleteEmployee, fetchEmployee, updateEmployee } from "../api/employees";
import { addCompensationRecord, fetchCompensationHistory } from "../api/compensation";
import useLookups from "../hooks/useLookups";
import EmployeeForm from "../components/employees/EmployeeForm";
import CompensationHistoryTable from "../components/employees/CompensationHistoryTable";
import AddCompensationForm from "../components/employees/AddCompensationForm";

export default function EmployeeDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { departments, countries } = useLookups();

  const [employee, setEmployee] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [showAddRaise, setShowAddRaise] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    return Promise.all([fetchEmployee(id), fetchCompensationHistory(id)])
      .then(([employeeData, historyData]) => {
        setEmployee(employeeData);
        setHistory(historyData);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleSave(values) {
    setSubmitting(true);
    setError(null);
    try {
      await updateEmployee(id, {
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        departmentId: Number(values.departmentId),
        countryId: Number(values.countryId),
        jobTitle: values.jobTitle,
        employmentStatus: values.employmentStatus,
      });
      await load();
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to save changes");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAddCompensation(payload) {
    await addCompensationRecord(id, payload);
    setShowAddRaise(false);
    await load();
  }

  async function handleDelete() {
    await deleteEmployee(id);
    navigate("/employees");
  }

  if (loading || !employee) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, md: 7 }}>
        <Paper sx={{ p: 3 }}>
          <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <Typography variant="h5" sx={{ fontWeight: 600 }}>
              {employee.firstName} {employee.lastName}
            </Typography>
            <Typography color="text.secondary">{employee.employeeCode}</Typography>
          </Stack>
          <EmployeeForm
            mode="edit"
            initialValues={{
              firstName: employee.firstName,
              lastName: employee.lastName,
              email: employee.email,
              departmentId: employee.department.id,
              countryId: employee.country.id,
              jobTitle: employee.jobTitle,
              employmentStatus: employee.employmentStatus,
            }}
            departments={departments}
            countries={countries}
            submitting={submitting}
            error={error}
            onSubmit={handleSave}
          />
          <Divider sx={{ my: 3 }} />
          <Button color="error" variant="outlined" onClick={() => setConfirmDelete(true)}>
            Delete employee
          </Button>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, md: 5 }}>
        <Paper sx={{ p: 3 }}>
          <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Compensation History
            </Typography>
            {!showAddRaise && (
              <Button size="small" onClick={() => setShowAddRaise(true)}>
                Add raise
              </Button>
            )}
          </Stack>
          {showAddRaise && (
            <AddCompensationForm
              defaultCurrency={employee.country.currencyCode}
              onSubmit={handleAddCompensation}
              onCancel={() => setShowAddRaise(false)}
            />
          )}
          <Divider sx={{ my: 2 }} />
          <CompensationHistoryTable history={history} />
        </Paper>
      </Grid>

      <Dialog open={confirmDelete} onClose={() => setConfirmDelete(false)}>
        <DialogTitle>Delete {employee.firstName} {employee.lastName}?</DialogTitle>
        <DialogContent>This will permanently remove the employee and their compensation history.</DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDelete(false)}>Cancel</Button>
          <Button color="error" onClick={handleDelete}>
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Grid>
  );
}
