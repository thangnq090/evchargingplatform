import { Routes, Route, Navigate } from "react-router-dom";
import { Layout } from "./app/layout/Layout";
import { Login } from "./features/auth/components/Login";
import { ProtectedRoute } from "./app/routes/ProtectedRoute";
import { AdminDashboardPage } from "./features/admin/pages/AdminDashboardPage";
import { VendorManagementPage } from "./features/admin/pages/VendorManagementPage";
import { UserGovernancePage } from "./features/admin/pages/UserGovernancePage";
import { VendorAdminPage } from "./features/vendor-admin/pages/VendorAdminPage";
import { VendorUserOperationsPage } from "./features/vendor-user/pages/VendorUserOperationsPage";

import { CustomerRegister } from "./features/auth/components/CustomerRegister";
import { CustomerProfilePage } from "./features/customer/pages/CustomerProfilePage";
import { VehicleManagementPage } from "./features/customer/pages/VehicleManagementPage";
import { ActiveSessionPage } from "./features/customer/pages/ActiveSessionPage";
import { CustomerSessionsPage } from "./features/customer/pages/CustomerSessionsPage";

import { useAuth } from "./features/auth/hooks/useAuth";

function RootRedirect() {
  const { user } = useAuth();
  if (user?.role === "ROLE_CUSTOMER") return <Navigate to="/customer/profile" replace />;
  if (user?.role === "ROLE_VENDOR_ADMIN") return <Navigate to="/vendor/portal" replace />;
  if (user?.role === "ROLE_VENDOR_USER") return <Navigate to="/vendor/operations" replace />;
  return <Navigate to="/admin/dashboard" replace />;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<CustomerRegister />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<RootRedirect />} />

        {/* Admin Routes */}
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute allowedRoles={["ROLE_ADMIN"]}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/vendors"
          element={
            <ProtectedRoute allowedRoles={["ROLE_ADMIN"]}>
              <VendorManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute allowedRoles={["ROLE_ADMIN"]}>
              <UserGovernancePage />
            </ProtectedRoute>
          }
        />

        {/* Vendor Routes */}
        <Route
          path="/vendor/portal"
          element={
            <ProtectedRoute allowedRoles={["ROLE_VENDOR_ADMIN", "ROLE_ADMIN"]}>
              <VendorAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/vendor/operations"
          element={
            <ProtectedRoute allowedRoles={["ROLE_VENDOR_ADMIN", "ROLE_VENDOR_USER", "ROLE_ADMIN"]}>
              <VendorUserOperationsPage />
            </ProtectedRoute>
          }
        />

        {/* Customer Routes */}
        <Route
          path="/customer/profile"
          element={
            <ProtectedRoute allowedRoles={["ROLE_CUSTOMER"]}>
              <CustomerProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/customer/vehicles"
          element={
            <ProtectedRoute allowedRoles={["ROLE_CUSTOMER"]}>
              <VehicleManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/customer/active-session"
          element={
            <ProtectedRoute allowedRoles={["ROLE_CUSTOMER"]}>
              <ActiveSessionPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/customer/sessions"
          element={
            <ProtectedRoute allowedRoles={["ROLE_CUSTOMER"]}>
              <CustomerSessionsPage />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="*" element={<RootRedirect />} />
    </Routes>
  );
}
