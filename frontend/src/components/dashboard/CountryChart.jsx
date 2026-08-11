import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import { BarChart } from "@mui/x-charts/BarChart";
import { SEQUENTIAL_BLUE } from "../../utils/chartTheme";

export default function CountryChart({ data }) {
  return (
    <Paper sx={{ p: 3, height: "100%" }}>
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
        Headcount by country
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Active employees
      </Typography>
      <BarChart
        dataset={data}
        xAxis={[{ scaleType: "band", dataKey: "countryName" }]}
        series={[{ dataKey: "headcount", label: "Headcount", color: SEQUENTIAL_BLUE }]}
        height={320}
        borderRadius={4}
        grid={{ horizontal: true }}
        slotProps={{ legend: { hidden: true } }}
      />
    </Paper>
  );
}
