import { useEffect, useRef, useState } from "react";

export default function DeleteAccountDialog({ isOpen, onCancel, onConfirm }) {
  const dialogRef = useRef(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (isOpen) {
      if (!dialogRef.current?.open) {
        dialogRef.current?.showModal();
      }
    } else if (dialogRef.current?.open) {
      dialogRef.current?.close();
    }
  }, [isOpen]);

  async function handleConfirm() {
    if (deleting) return;
    setDeleting(true);
    try {
      await onConfirm();
    } catch (err) {
      console.error("Failed to delete account:", err);
      setDeleting(false);
    }
  }

  function handleCancel() {
    if (deleting) return;
    dialogRef.current?.close();
  }

  return (
    <dialog
      ref={dialogRef}
      onClose={() => {
        // Runs on every close path: the Cancel button, Escape, or a close()
        // triggered by the parent. Reset the in-flight guard so the next open
        // starts clean.
        setDeleting(false);
        onCancel();
      }}
      className="m-auto w-96 max-w-[90vw] rounded-2xl p-6 shadow-xl backdrop:bg-slate-900/20"
    >
      <div className="flex flex-col gap-4">
        <h2 className="text-lg font-semibold text-slate-800">Delete account</h2>
        <p className="text-sm text-slate-600">
          This permanently deletes your account along with every project you
          own. This cannot be undone.
        </p>
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={handleCancel}
            disabled={deleting}
            className="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-100 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={deleting}
            className="rounded-lg bg-red-500 px-4 py-2 text-sm font-medium text-white hover:bg-red-600 disabled:opacity-50 disabled:hover:bg-red-500"
          >
            {deleting ? "Deleting…" : "Delete account"}
          </button>
        </div>
      </div>
    </dialog>
  );
}
