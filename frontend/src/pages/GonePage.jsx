import { Link } from "react-router-dom";
import AppHeader from "../ui/layout/AppHeader.jsx";

export default function GonePage({ isLoggedIn, user }) {
  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <AppHeader isLoggedIn={isLoggedIn} user={user} hideAuth />

      <main className="flex flex-1 items-center justify-center px-6 py-10">
        <div className="w-full max-w-sm text-center">
          <h1 className="text-2xl font-semibold text-slate-800">
            Reset link expired
          </h1>
          <p className="mt-2 text-sm text-slate-600">
            This password reset link has expired. Password reset links are valid
            for 10 minutes. You can request a new one below.
          </p>
          <div className="mt-6 flex flex-col gap-2">
            <Link
              to="/forgot-password"
              className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-fuchsia-600"
            >
              Request a new link
            </Link>
            <Link
              to="/login"
              className="text-sm font-medium text-fuchsia-500 hover:text-fuchsia-600"
            >
              Back to log in
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
}