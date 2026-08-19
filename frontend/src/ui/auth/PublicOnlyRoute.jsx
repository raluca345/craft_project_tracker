import { Navigate } from "react-router-dom";

// Guards a route that should only be seen by logged-out visitors (login/signup).
// If a user is already logged in, send them to the app instead.
export default function PublicOnlyRoute({
  isLoggedIn,
  redirectTo = "/home",
  children,
}) {
  return isLoggedIn ? <Navigate to={redirectTo} replace /> : children;
}