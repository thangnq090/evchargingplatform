import { Routes, Route, Navigate } from "react-router-dom";
import { Layout } from "./app/layout/Layout";
import { Dashboard } from "./pages/Dashboard";
import { Stations } from "./pages/Stations";
import { Sessions } from "./pages/Sessions";
import { Login } from "./features/auth/components/Login";
import { ProtectedRoute } from "./app/routes/ProtectedRoute";

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="stations" element={<Stations />} />
        <Route path="sessions" element={<Sessions />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
