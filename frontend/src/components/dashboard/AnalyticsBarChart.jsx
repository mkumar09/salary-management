import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import { BarChart } from "@mui/x-charts/BarChart";
import { SEQUENTIAL_BLUE } from "../../utils/chartTheme";

/**
 * One measure per chart (headcount, average salary, ...) plotted against a category axis
 * (department, country, salary band). Single consistent hue since color encodes "series" and
 * there's only one here - a legend/per-bar color would be noise, not signal. See dataviz notes
 * in utils/chartTheme.js.
 */
// MUI X Charts' tooltip calls series.valueFormatter unconditionally when the key is present -
// an `undefined` formatter (rather than the key being absent) throws on hover, so default it.
const defaultValueFormatter = (value) => value?.toLocaleString() ?? "";

export default function AnalyticsBarChart({
  title,
  subtitle,
  data,
  xKey,
  valueKey,
  valueLabel,
  valueFormatter = defaultValueFormatter,
}) {
  return (
    <Paper sx={{ p: 3, height: "100%" }}>
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
        {title}
      </Typography>
      {subtitle && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {subtitle}
        </Typography>
      )}
      <BarChart
        dataset={data}
        xAxis={[
          {
            scaleType: "band",
            dataKey: xKey,
            // Longer category names (e.g. "Human Resources") collide with their neighbors at
            // this chart's width - MUI silently drops the overlapping label rather than
            // overlapping text, so angle every label to guarantee all categories stay visible.
            tickLabelStyle: { angle: -30, textAnchor: "end" },
          },
        ]}
        series={[{ dataKey: valueKey, label: valueLabel, color: SEQUENTIAL_BLUE, valueFormatter }]}
        height={340}
        margin={{ bottom: 70 }}
        borderRadius={4}
        grid={{ horizontal: true }}
        slotProps={{ legend: { hidden: true } }}
      />
    </Paper>
  );
}
