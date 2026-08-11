import { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import { fetchByCountry, fetchByDepartment, fetchDistribution, fetchSummary } from "../api/analytics";
import SummaryCards from "../components/dashboard/SummaryCards";
import AnalyticsBarChart from "../components/dashboard/AnalyticsBarChart";
import { formatCompactUsd } from "../utils/format";

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [byDepartment, setByDepartment] = useState([]);
  const [byCountry, setByCountry] = useState([]);
  const [distribution, setDistribution] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setError(null);
    Promise.all([fetchSummary(), fetchByDepartment(), fetchByCountry(), fetchDistribution()])
      .then(([summaryData, departmentData, countryData, distributionData]) => {
        if (cancelled) return;
        setSummary(summaryData);
        setByDepartment(departmentData);
        setByCountry(countryData);
        setDistribution(distributionData);
      })
      .catch(() => {
        if (!cancelled) setError("Failed to load analytics. Is the backend running?");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (loading || !summary) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (summary.headcount === 0) {
    return <Typography color="text.secondary">No employees yet. Add one to see analytics.</Typography>;
  }

  return (
    <Box>
      <SummaryCards summary={summary} />
      <Grid container spacing={3}>
        {/*
          Full width: department has 7 categories including long names ("Human Resources",
          "Customer Support") that collide at half-width, and MUI silently drops overlapping
          labels rather than showing them squeezed. Country (6, mostly shorter names) fits at
          half-width below.
        */}
        <Grid size={12}>
          <AnalyticsBarChart
            title="Average salary by department"
            subtitle="USD-equivalent, active employees"
            data={byDepartment}
            xKey="departmentName"
            valueKey="averageSalaryUsd"
            valueLabel="Average salary"
            valueFormatter={formatCompactUsd}
          />
        </Grid>
        <Grid size={12}>
          <AnalyticsBarChart
            title="Headcount by department"
            subtitle="Active employees"
            data={byDepartment}
            xKey="departmentName"
            valueKey="headcount"
            valueLabel="Headcount"
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <AnalyticsBarChart
            title="Average salary by country"
            subtitle="USD-equivalent, active employees"
            data={byCountry}
            xKey="countryName"
            valueKey="averageSalaryUsd"
            valueLabel="Average salary"
            valueFormatter={formatCompactUsd}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <AnalyticsBarChart
            title="Headcount by country"
            subtitle="Active employees"
            data={byCountry}
            xKey="countryName"
            valueKey="headcount"
            valueLabel="Headcount"
          />
        </Grid>
        <Grid size={12}>
          <AnalyticsBarChart
            title="Salary distribution"
            subtitle="Active employees, USD-equivalent bands"
            data={distribution}
            xKey="bucketLabel"
            valueKey="count"
            valueLabel="Employees"
          />
        </Grid>
      </Grid>
    </Box>
  );
}
