import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/apiAuth.js";
import { getErrorMessage } from "../commons/errors.js";
import FormField from "../ui/forms/FormField.jsx";
import AppHeader from "../ui/layout/AppHeader.jsx";

const INPUT_STYLE =
  "rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none";

export default function LoginPage({ isLoggedIn, user, onLogin }) {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    try {
      const data = await login(email, password);
      onLogin(data);
      navigate("/home");
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <AppHeader isLoggedIn={isLoggedIn} user={user} hideAuth />

      <main className="flex flex-1 items-center justify-center px-6 py-10">
        <form
          onSubmit={handleSubmit}
          className="flex w-full max-w-sm flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <div>
            <h1 className="text-xl font-semibold text-slate-800">
              Good to see you again
            </h1>
            <p className="mt-1 text-sm text-slate-600">
              Your board&apos;s waiting. Nothing&apos;s changed.
            </p>
          </div>

          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          )}

          <FormField label="Email">
            <input
              type="email"
              id="login-email"
              autoComplete="email"
              className={INPUT_STYLE}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </FormField>

          <FormField label="Password">
            <input
              type="password"
              id="login-password"
              autoComplete="current-password"
              className={INPUT_STYLE}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </FormField>

          <button
            type="submit"
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-fuchsia-600"
          >
            Log in
          </button>

          <p className="text-center text-sm text-slate-500">
            New here?{" "}
            <Link to="/signup" className="font-medium text-fuchsia-500 hover:text-fuchsia-600">
              Make an account
            </Link>
          </p>
        </form>
      </main>
    </div>
  );
}
