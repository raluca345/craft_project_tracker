import { useState, useRef, useEffect } from "react";
import { Link } from "react-router-dom";
import Avatar from "./Avatar.jsx";

export default function AuthActions({ isLoggedIn, user, onLogout }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    function handleClick(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  if (isLoggedIn) {
    return (
      <div ref={ref} className="relative">
        <button
          type="button"
          onClick={() => setOpen((o) => !o)}
          className="rounded-full focus:outline-none focus:ring-2 focus:ring-fuchsia-400 focus:ring-offset-2"
          aria-label="Open account menu"
        >
          <Avatar user={user} size="h-15 w-15" />
        </button>
        {open && (
          <div className="absolute left-1/2 z-50 mt-4 w-56 -translate-x-1/2 rounded-lg border border-slate-200 bg-white py-1 shadow-lg">
            <div className="border-b border-slate-100 px-4 py-3">
              <p className="text-sm font-semibold text-slate-800">
                {user.name}
              </p>
              <p className="text-xs text-slate-500">{user.email}</p>
            </div>
            <Link
              to="/settings"
              onClick={() => setOpen(false)}
              className="block px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
            >
              Settings
            </Link>
            <button
              type="button"
              onClick={() => {
                setOpen(false);
                onLogout();
              }}
              className="block w-full px-4 py-2 text-left text-sm text-slate-700 hover:bg-slate-50"
            >
              Log out
            </button>
          </div>
        )}
      </div>
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
