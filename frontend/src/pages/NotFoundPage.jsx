import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <Stack spacing={2} sx={{ py: 8, alignItems: "center" }}>
      <Typography variant="h5">Page not found</Typography>
      <Button component={Link} to="/" variant="contained">
        Back to dashboard
      </Button>
    </Stack>
  );
}
