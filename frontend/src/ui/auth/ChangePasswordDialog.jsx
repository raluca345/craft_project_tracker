import { useState, useEffect, useRef } from "react";
import { toast } from "sonner";
import { getErrorMessage } from "../../commons/errors.js";
import PasswordField from "../forms/PasswordField.jsx";

function validate({ currentPassword, newPassword, confirmPassword }) {
  const errors = {};
  if (!currentPassword) errors.currentPassword = "Enter your current password.";
  if (!newPassword) errors.newPassword = "Enter a new password.";
  else if (newPassword.length < 8)
    errors.newPassword = "Password must be at least 8 characters.";
  if (!confirmPassword) errors.confirmPassword = "Confirm your new password.";
  else if (newPassword && newPassword !== confirmPassword)
    errors.confirmPassword = "Passwords do not match.";
  if (newPassword && newPassword === currentPassword)
    errors.newPassword =
      "Your new password must be different from your current one.";
  return errors;
}

export default function ChangePasswordDialog({ isOpen, onCancel, onSave }) {
  const dialogRef = useRef(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errors, setErrors] = useState({});
  const [error, setError] = useState(null);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (isOpen && !dialogRef.current?.open) dialogRef.current?.showModal();
    else if (!isOpen && dialogRef.current?.open) dialogRef.current?.close();
  }, [isOpen]);

  function resetForm() {
    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
    setErrors({});
    setError(null);
  }

  function handleCancel() {
    resetForm();
    onCancel();
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (isSaving) return;

    setError(null);
    const nextErrors = validate({
      currentPassword,
      newPassword,
      confirmPassword,
    });
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setIsSaving(true);
    try {
      await onSave(currentPassword, newPassword);
      // Fired before close() so it's queued while the dialog is still mounted;
      // the Toaster renders at app root, outside the dialog's top layer.
      toast.success("Your password has been changed.");
      resetForm();
      dialogRef.current?.close();
    } catch (err) {
      console.error("Failed to change password:", err);
      setError(getErrorMessage(err));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <dialog
      ref={dialogRef}
      onClose={handleCancel}
      className="m-auto w-96 max-w-[90vw] rounded-2xl p-6 shadow-xl backdrop:bg-slate-900/20"
    >
      <form className="flex flex-col gap-4" onSubmit={handleSubmit} noValidate>
        <h2 className="text-lg font-semibold text-slate-800">
          Change your Password
        </h2>

        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}

        <PasswordField
          label="Current Password"
          id="current-password"
          autoComplete="current-password"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          error={errors.currentPassword}
        />
        <PasswordField
          label="New Password"
          id="new-password"
          autoComplete="new-password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          error={errors.newPassword}
        />
        <PasswordField
          label="Confirm Password"
          id="confirm-password"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          error={errors.confirmPassword}
        />

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={handleCancel}
            disabled={isSaving}
            className="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-100 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSaving}
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white hover:bg-fuchsia-600 disabled:opacity-50 disabled:hover:bg-fuchsia-500"
          >
            {isSaving ? "Saving…" : "Save"}
          </button>
        </div>
      </form>
    </dialog>
  );
}
