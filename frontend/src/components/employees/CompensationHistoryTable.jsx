import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import Chip from "@mui/material/Chip";
import Typography from "@mui/material/Typography";
import { formatDate, formatMoney } from "../../utils/format";

const REASON_COLOR = { HIRE: "default", RAISE: "success", ADJUSTMENT: "info" };

export default function CompensationHistoryTable({ history }) {
  if (history.length === 0) {
    return <Typography color="text.secondary">No compensation history yet.</Typography>;
  }

  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell>Effective Date</TableCell>
          <TableCell>Amount</TableCell>
          <TableCell>Reason</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {history.map((record) => (
          <TableRow key={record.id}>
            <TableCell>{formatDate(record.effectiveDate)}</TableCell>
            <TableCell>{formatMoney(record.amount, record.currencyCode)}</TableCell>
            <TableCell>
              <Chip size="small" label={record.reason} color={REASON_COLOR[record.reason] ?? "default"} />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
