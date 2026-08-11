import { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import { fetchByCountry, fetchByDepartment, fetchDistribution, fetchSummary } from "../api/analytics";
import SummaryCards from "../components/dashboard/SummaryCards";
import DepartmentChart from "../components/dashboard/DepartmentChart";
import CountryChart from "../components/dashboard/CountryChart";
import DistributionChart from "../components/dashboard/DistributionChart";

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
        <Grid size={{ xs: 12, md: 6 }}>
          <DepartmentChart data={byDepartment} />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <CountryChart data={byCountry} />
        </Grid>
        <Grid size={12}>
          <DistributionChart data={distribution} />
        </Grid>
      </Grid>
    </Box>
  );
}
