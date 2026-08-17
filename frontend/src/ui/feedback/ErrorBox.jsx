export default function ErrorBox({ message, onDismiss }) {
  if (!message) return null;

  return (
    <div
      role="alert"
      className="fixed top-4 left-1/2 z-50 flex w-[calc(100%-2rem)] max-w-md -translate-x-1/2 items-center gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 shadow-lg"
    >
      <span className="flex-1 text-sm font-medium text-red-800">{message}</span>
      <button
        type="button"
        onClick={onDismiss}
        aria-label="Dismiss error"
        className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-red-400 transition-colors hover:bg-red-100 hover:text-red-600"
      >
        ×
      </button>
    </div>
  );
}
