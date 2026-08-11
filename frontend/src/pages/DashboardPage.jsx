import { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Typography from "@mui/material/Typography";
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

  useEffect(() => {
    let cancelled = false;
    Promise.all([fetchSummary(), fetchByDepartment(), fetchByCountry(), fetchDistribution()])
      .then(([summaryData, departmentData, countryData, distributionData]) => {
        if (cancelled) return;
        setSummary(summaryData);
        setByDepartment(departmentData);
        setByCountry(countryData);
        setDistribution(distributionData);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

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
