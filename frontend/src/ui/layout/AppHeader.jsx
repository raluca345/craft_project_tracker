import { Link } from "react-router-dom";
import AuthActions from "../auth/AuthActions.jsx";

export default function AppHeader({
  isLoggedIn,
  user,
  onLogout,
  hideAuth,
  children,
}) {
  return (
    <header className="flex items-center justify-between gap-6 px-6 py-4">
      <Link
        to={isLoggedIn ? "/home" : "/"}
        className="text-2xl font-bold text-(--accent-color2)"
      >
        Yarn Board
      </Link>
      <div className="flex items-center gap-5">
        {children}
        {!hideAuth && (
          <AuthActions isLoggedIn={isLoggedIn} user={user} onLogout={onLogout} />
        )}
      </div>
    </header>
  );
}
