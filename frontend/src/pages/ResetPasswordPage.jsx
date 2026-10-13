import { useState, useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { resetPassword, verifyResetToken } from "../api/apiAuth.js";
import { ApiError } from "../api/apiCore.js";
import { getErrorMessage } from "../commons/errors.js";
import PasswordField from "../ui/forms/PasswordField.jsx";
import AppHeader from "../ui/layout/AppHeader.jsx";

export default function ResetPasswordPage({ isLoggedIn, user }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      navigate("/forgot-password");
      return;
    }

    async function checkToken() {
      try {
        await verifyResetToken(token);
      } catch (err) {
        if (err instanceof ApiError && (err.status === 410 || err.status === 404)) {
          navigate("/gone");
        }
      } finally {
        setLoading(false);
      }
    }

    checkToken();
  }, [token, navigate]);

  async function handleSubmit(e) {
    e.preventDefault();
    if (submitting) return;

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await resetPassword(token, password);
      navigate("/login");
    } catch (err) {
      console.error(err);
      if (err instanceof ApiError && (err.status === 410 || err.status === 404)) {
        navigate("/gone");
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
    }

  if (loading) {
    return null;
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
              Reset your password
            </h1>
            <p className="mt-1 text-sm text-slate-600">
              Enter a new password to continue.
            </p>
          </div>

          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          )}

          <PasswordField
            label="New password"
            id="reset-password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
          />

          <PasswordField
            label="Confirm new password"
            id="reset-password-confirm"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            minLength={8}
            required
          />

          <button
            type="submit"
            disabled={submitting}
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-fuchsia-600 disabled:opacity-50 disabled:hover:bg-fuchsia-500"
          >
            {submitting ? "Saving…" : "Save new password"}
          </button>

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