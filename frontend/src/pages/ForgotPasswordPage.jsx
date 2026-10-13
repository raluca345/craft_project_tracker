import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../api/apiAuth.js";
import { getErrorMessage } from "../commons/errors.js";
import FormField from "../ui/forms/FormField.jsx";
import AppHeader from "../ui/layout/AppHeader.jsx";

const INPUT_STYLE =
  "rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none";

export default function ForgotPasswordPage({ isLoggedIn, user }) {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      await forgotPassword(email);
      setSubmitted(true);
    } catch (err) {
      console.error(err);
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
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
              Forgot your password?
            </h1>
            <p className="mt-1 text-sm text-slate-600">
              {submitted
                ? "Check your email for a reset link."
                : "Enter your email and we'll send you a reset link."}
            </p>
          </div>

          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          )}

          {!submitted && (
            <>
              <FormField label="Email">
                <input
                  type="email"
                  id="forgot-email"
                  autoComplete="email"
                  className={INPUT_STYLE}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </FormField>

              <button
                type="submit"
                disabled={submitting}
                className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-fuchsia-600 disabled:opacity-50 disabled:hover:bg-fuchsia-500"
              >
                {submitting ? "Sending…" : "Send reset link"}
              </button>
            </>
          )}

          <p className="text-center text-sm text-slate-500">
            <Link
              to="/login"
              className="font-medium text-fuchsia-500 hover:text-fuchsia-600"
            >
              Back to log in
            </Link>
          </p>
        </form>
      </main>
    </div>
  );
}
