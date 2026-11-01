export default function FormField({ label, error, id, children }) {
  return (
    <span className="flex flex-col gap-1">
      <label className="text-sm text-slate-600" htmlFor={id}>
        {label}
      </label>
      {children}
      {error && <span className="text-xs text-red-500">{error}</span>}
    </span>
  );
}
