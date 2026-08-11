import { useState } from "react";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";

export default function AddCompensationForm({ defaultCurrency, onSubmit, onCancel }) {
  const [amount, setAmount] = useState("");
  const [currencyCode, setCurrencyCode] = useState(defaultCurrency);
  const [effectiveDate, setEffectiveDate] = useState(new Date().toISOString().slice(0, 10));
  const [reason, setReason] = useState("RAISE");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({ amount: Number(amount), currencyCode, effectiveDate, reason });
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to add compensation record");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack spacing={2} sx={{ mt: 2 }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField
          select
          label="Reason"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          size="small"
        >
          <MenuItem value="RAISE">Raise</MenuItem>
          <MenuItem value="ADJUSTMENT">Adjustment</MenuItem>
        </TextField>
        <TextField
          label="New amount"
          type="number"
          required
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          size="small"
        />
        <TextField
          label="Currency"
          required
          value={currencyCode}
          onChange={(e) => setCurrencyCode(e.target.value.toUpperCase())}
          size="small"
          slotProps={{ htmlInput: { maxLength: 3 } }}
        />
        <TextField
          label="Effective date"
          type="date"
          required
          value={effectiveDate}
          onChange={(e) => setEffectiveDate(e.target.value)}
          size="small"
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <Stack direction="row" spacing={2}>
          <Button type="submit" variant="contained" disabled={submitting}>
            Save
          </Button>
          <Button onClick={onCancel} disabled={submitting}>
            Cancel
          </Button>
        </Stack>
      </Stack>
    </form>
  );
}
