import { useEffect, useRef, useState } from "react";

const TEXTAREA_STYLE =
  "min-h-40 resize-y rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none";

export default function NotesModal({ project, onCancel, onSave }) {
  const dialogRef = useRef(null);
  const [notes, setNotes] = useState("");
  const [prevProject, setPrevProject] = useState(project);

  if (project !== prevProject) {
    setPrevProject(project);
    setNotes(project?.notes ?? "");
  }

  useEffect(() => {
    if (project) {
      if (!dialogRef.current?.open) {
        dialogRef.current?.showModal();
      }
    } else if (dialogRef.current?.open) {
      dialogRef.current?.close();
    }
  }, [project]);

  function handleSubmit(event) {
    event.preventDefault();
    if (!project) return;

    Promise.resolve(onSave(project.id, notes))
      .then(() => {
        dialogRef.current?.close();
      })
      .catch((err) => {
        console.error("Failed to save notes:", err);
      });
  }

  return (
    <dialog
      ref={dialogRef}
      onClose={onCancel}
      className="m-auto w-96 max-w-[90vw] rounded-2xl p-6 shadow-xl backdrop:bg-slate-900/20"
    >
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        <h2 className="text-lg font-semibold text-slate-800">Project Notes</h2>
        <textarea
          id="project-notes"
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
          className={TEXTAREA_STYLE}
          placeholder="Pattern notes, modifications, ideas..."
          autoFocus
        />
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={() => dialogRef.current?.close()}
            className="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="rounded-lg bg-fuchsia-500 px-4 py-2 text-sm font-medium text-white hover:bg-fuchsia-600"
          >
            Save
          </button>
        </div>
      </form>
    </dialog>
  );
}
