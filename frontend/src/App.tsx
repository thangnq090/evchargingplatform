import { Routes, Route, Navigate } from "react-router-dom";
import { Layout } from "./app/layout/Layout";
import { Login } from "./features/auth/components/Login";
import { ProtectedRoute } from "./app/routes/ProtectedRoute";
import { AdminDashboardPage } from "./features/admin/pages/AdminDashboardPage";
import { VendorManagementPage } from "./features/admin/pages/VendorManagementPage";
import { UserGovernancePage } from "./features/admin/pages/UserGovernancePage";
import { VendorAdminPage } from "./features/vendor-admin/pages/VendorAdminPage";
import { VendorUserOperationsPage } from "./features/vendor-user/pages/VendorUserOperationsPage";

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
        <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
        <Route path="/admin/vendors" element={<VendorManagementPage />} />
        <Route path="/vendor/portal" element={<VendorAdminPage />} />
        <Route path="/vendor/operations" element={<VendorUserOperationsPage />} />
        <Route path="/admin/users" element={<UserGovernancePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
    </Routes>
  );
}
