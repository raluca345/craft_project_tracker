import { Link } from "react-router-dom";
import FormField from "../ui/FormField.jsx";

const INPUT_STYLE =
  "rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none";

// TODO: implement real login. Backend has no auth endpoints yet; the form below
// is UI-only for now.
export default function LoginPage() {
  function handleSubmit(e) {
    e.preventDefault();
  }

  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <header className="flex items-center justify-between px-6 py-4">
        <Link
          to="/"
          className="flex items-center gap-2 text-sm font-semibold text-(--accent-color2)"
        >
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 text-base shadow-sm">
            🧶
          </span>
          Craft Project Tracker
        </Link>
        <Link
          to="/signup"
          className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-amber-100 hover:text-slate-800"
        >
          Sign up
        </Link>
      </header>

      <main className="flex flex-1 items-center justify-center px-6 py-10">
        <form
          onSubmit={handleSubmit}
          className="flex w-full max-w-sm flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <div>
            <h1 className="text-xl font-semibold text-slate-800">Welcome back</h1>
            <p className="mt-1 text-sm text-slate-600">
              Log in to get back to your projects.
            </p>
          </div>

          <FormField label="Email">
            <input
              type="email"
              id="login-email"
              autoComplete="email"
              className={INPUT_STYLE}
            />
          </FormField>

          <FormField label="Password">
            <input
              type="password"
              id="login-password"
              autoComplete="current-password"
              className={INPUT_STYLE}
            />
          </FormField>

          <button
            type="submit"
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-fuchsia-600"
          >
            Log in
          </button>
        </form>
      </main>
    </div>
  );
}