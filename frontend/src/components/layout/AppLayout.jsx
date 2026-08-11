import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Tabs from "@mui/material/Tabs";
import Tab from "@mui/material/Tab";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

export default function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();

  const currentTab = location.pathname.startsWith("/employees") ? "/employees" : "/";

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar position="static" color="primary" elevation={0}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 600 }}>
            ACME Salary Management
          </Typography>
        </Toolbar>
        <Tabs
          value={currentTab}
          onChange={(_, value) => navigate(value)}
          textColor="inherit"
          indicatorColor="secondary"
          sx={{ px: 2 }}
        >
          <Tab label="Dashboard" value="/" />
          <Tab label="Employees" value="/employees" />
        </Tabs>
      </AppBar>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
