import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { DataGrid } from "@mui/x-data-grid";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import Chip from "@mui/material/Chip";
import { fetchEmployees } from "../api/employees";
import useLookups from "../hooks/useLookups";
import useDebouncedValue from "../hooks/useDebouncedValue";
import EmployeeFilters from "../components/employees/EmployeeFilters";
import { formatMoney } from "../utils/format";

const DEFAULT_FILTERS = { search: "", departmentId: "", countryId: "", status: "" };

export default function EmployeesPage() {
  const navigate = useNavigate();
  const { departments, countries } = useLookups();
  const [filters, setFilters] = useState(DEFAULT_FILTERS);
  const debouncedSearch = useDebouncedValue(filters.search, 350);
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 25 });
  const [rows, setRows] = useState([]);
  const [rowCount, setRowCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setPaginationModel((prev) => ({ ...prev, page: 0 }));
  }, [debouncedSearch, filters.departmentId, filters.countryId, filters.status]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchEmployees({
      page: paginationModel.page,
      size: paginationModel.pageSize,
      search: debouncedSearch || undefined,
      departmentId: filters.departmentId || undefined,
      countryId: filters.countryId || undefined,
      status: filters.status || undefined,
    })
      .then((data) => {
        if (cancelled) return;
        setRows(data.content);
        setRowCount(data.totalElements);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [paginationModel, debouncedSearch, filters.departmentId, filters.countryId, filters.status]);

  const columns = useMemo(
    () => [
      { field: "employeeCode", headerName: "Code", width: 120 },
      {
        field: "name",
        headerName: "Name",
        flex: 1,
        minWidth: 180,
        valueGetter: (_value, row) => `${row.firstName} ${row.lastName}`,
      },
      { field: "jobTitle", headerName: "Title", flex: 1, minWidth: 180 },
      {
        field: "department",
        headerName: "Department",
        width: 160,
        valueGetter: (_value, row) => row.department?.name,
      },
      {
        field: "country",
        headerName: "Country",
        width: 140,
        valueGetter: (_value, row) => row.country?.name,
      },
      {
        field: "salary",
        headerName: "Current Salary",
        width: 160,
        valueGetter: (_value, row) => row.currentSalaryAmount,
        renderCell: (params) => formatMoney(params.row.currentSalaryAmount, params.row.currentSalaryCurrency),
      },
      {
        field: "employmentStatus",
        headerName: "Status",
        width: 130,
        renderCell: (params) => (
          <Chip
            size="small"
            label={params.value}
            color={params.value === "ACTIVE" ? "success" : "default"}
            variant="outlined"
          />
        ),
      },
    ],
    [],
  );

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
        Employees
      </Typography>
      <EmployeeFilters filters={filters} onChange={setFilters} departments={departments} countries={countries} />
      <div style={{ height: 600, width: "100%" }}>
        <DataGrid
          rows={rows}
          columns={columns}
          rowCount={rowCount}
          loading={loading}
          paginationMode="server"
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[25, 50, 100]}
          disableRowSelectionOnClick
          onRowClick={(params) => navigate(`/employees/${params.id}`)}
          sx={{ cursor: "pointer" }}
        />
      </div>
    </Paper>
  );
}
