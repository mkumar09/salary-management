import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import { formatCompactNumber, formatCompactUsd } from "../../utils/format";

function StatTile({ label, value }) {
  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
        {label}
      </Typography>
      <Typography variant="h4" sx={{ fontWeight: 600 }}>
        {value}
      </Typography>
    </Paper>
  );
}

export default function SummaryCards({ summary }) {
  return (
    <Grid container spacing={3} sx={{ mb: 3 }}>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatTile label="Headcount" value={formatCompactNumber(summary.headcount)} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatTile label="Total payroll (USD)" value={formatCompactUsd(summary.totalPayrollUsd)} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatTile label="Average salary (USD)" value={formatCompactUsd(summary.averageSalaryUsd)} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatTile label="Median salary (USD)" value={formatCompactUsd(summary.medianSalaryUsd)} />
      </Grid>
    </Grid>
  );
}
