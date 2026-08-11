import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Button from "@mui/material/Button";
import AddIcon from "@mui/icons-material/Add";
import { useNavigate } from "react-router-dom";

const STATUS_OPTIONS = [
  { value: "", label: "All statuses" },
  { value: "ACTIVE", label: "Active" },
  { value: "TERMINATED", label: "Terminated" },
];

export default function EmployeeFilters({ filters, onChange, departments, countries }) {
  const navigate = useNavigate();

  function set(field, value) {
    onChange({ ...filters, [field]: value });
  }

  return (
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 3, alignItems: { sm: "center" } }}>
      <TextField
        label="Search name, email, or code"
        value={filters.search}
        onChange={(e) => set("search", e.target.value)}
        size="small"
        sx={{ minWidth: 260 }}
      />
      <TextField
        select
        label="Department"
        value={filters.departmentId}
        onChange={(e) => set("departmentId", e.target.value)}
        size="small"
        sx={{ minWidth: 180 }}
      >
        <MenuItem value="">All departments</MenuItem>
        {departments.map((d) => (
          <MenuItem key={d.id} value={d.id}>
            {d.name}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        select
        label="Country"
        value={filters.countryId}
        onChange={(e) => set("countryId", e.target.value)}
        size="small"
        sx={{ minWidth: 180 }}
      >
        <MenuItem value="">All countries</MenuItem>
        {countries.map((c) => (
          <MenuItem key={c.id} value={c.id}>
            {c.name}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        select
        label="Status"
        value={filters.status}
        onChange={(e) => set("status", e.target.value)}
        size="small"
        sx={{ minWidth: 160 }}
      >
        {STATUS_OPTIONS.map((opt) => (
          <MenuItem key={opt.value} value={opt.value}>
            {opt.label}
          </MenuItem>
        ))}
      </TextField>
      <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate("/employees/new")} sx={{ ml: { sm: "auto" } }}>
        Add Employee
      </Button>
    </Stack>
  );
}
