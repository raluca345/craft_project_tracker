import { Link } from "react-router-dom";
import AuthActions from "../auth/AuthActions.jsx";

export default function AppHeader({ isLoggedIn, user, hideAuth }) {
  return (
    <header className="flex items-center justify-between px-6 py-4">
      <Link
        to={isLoggedIn ? "/home" : "/"}
        className="flex items-center gap-2 text-sm font-semibold text-(--accent-color2)"
      >
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 text-sm font-bold shadow-sm">
          C
        </span>
        Craft Project Tracker
      </Link>
      {!hideAuth && <AuthActions isLoggedIn={isLoggedIn} user={user} />}
    </header>
  );
}
