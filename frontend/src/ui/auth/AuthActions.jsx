import { Link } from "react-router-dom";
import DefaultAvatar from "./DefaultAvatar.jsx";

export default function AuthActions({ isLoggedIn, user }) {
  if (isLoggedIn) {
    return (
      <Link
        to="/home"
        className="rounded-full focus:outline-none focus:ring-2 focus:ring-fuchsia-400 focus:ring-offset-2"
        aria-label="Open account"
      >
        <DefaultAvatar user={user} size="h-9 w-9" />
      </Link>
    );
  }

  return (
    <nav className="flex items-center gap-2" aria-label="Account">
      <Link
        to="/login"
        className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-amber-100 hover:text-slate-800"
      >
        Log in
      </Link>
      <Link
        to="/signup"
        className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-fuchsia-600"
      >
        Sign up
      </Link>
    </nav>
  );
}
