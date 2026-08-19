import { Navigate } from "react-router-dom";

// Guards a route that requires an authenticated user. If no user is logged in,
// redirect to the login page instead of rendering the protected content.
export default function ProtectedRoute({
  isLoggedIn,
  redirectTo = "/login",
  children,
}) {
  return isLoggedIn ? children : <Navigate to={redirectTo} replace />;
}