import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../features/auth/hooks/useAuth";

interface ProtectedRouteProps {
  children?: React.ReactNode;
  allowedRoles?: string[];
}

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && user?.role) {
    if (!allowedRoles.includes(user.role)) {
      // Role-aware fallback redirect
      if (user.role === "ROLE_CUSTOMER") {
        return <Navigate to="/customer/profile" replace />;
      }
      if (user.role === "ROLE_VENDOR_ADMIN") {
        return <Navigate to="/vendor/portal" replace />;
      }
      if (user.role === "ROLE_VENDOR_USER") {
        return <Navigate to="/vendor/operations" replace />;
      }
      return <Navigate to="/admin/dashboard" replace />;
    }
  }

  return children ? <>{children}</> : <Outlet />;
}
