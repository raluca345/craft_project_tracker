import { useEffect, useRef } from "react";

export default function ConfirmDialog({ project, onCancel, onConfirm }) {
  const dialogRef = useRef(null);

  useEffect(() => {
    if (project) {
      if (!dialogRef.current?.open) {
        dialogRef.current?.showModal();
      }
    } else if (dialogRef.current?.open) {
      dialogRef.current?.close();
    }
  }, [project]);

  return (
    <dialog
      ref={dialogRef}
      onClose={onCancel}
      className="m-auto w-96 max-w-[90vw] rounded-2xl p-6 shadow-xl backdrop:bg-slate-900/20"
    >
      <div className="flex flex-col gap-4">
        <h2 className="text-lg font-semibold text-slate-800">Delete Project</h2>
        <p className="text-sm text-slate-600">
          Delete "{project?.patternName}"? This cannot be undone.
        </p>
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={() => dialogRef.current?.close()}
            className="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => {
              Promise.resolve(onConfirm(project))
                .then(() => dialogRef.current?.close())
                .catch((err) => {
                  console.error("Failed to delete project:", err);
                });
            }}
            className="rounded-lg bg-red-500 px-4 py-2 text-sm font-medium text-white hover:bg-red-600"
          >
            Delete
          </button>
        </div>
      </div>
    </dialog>
  );
}
