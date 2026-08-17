import FormField from "./FormField";
import { useState } from "react";

export default function AutocompleteField({
  label,
  value,
  onChange,
  suggestions,
  error,
}) {
  const [query, setQuery] = useState(value ?? "");
  const [prevValue, setPrevValue] = useState(value);

  if (value !== prevValue) {
    setPrevValue(value);
    setQuery(value ?? "");
  }

  const filtered = suggestions.filter((s) =>
    s.toLowerCase().includes(query.toLowerCase()),
  );

  return (
    <FormField label={label} error={error}>
      <input
        value={query}
        onChange={(e) => {
          setQuery(e.target.value);
          onChange(e.target.value);
        }}
        list={`${label}-options`}
        aria-invalid={error ? true : undefined}
        className={`rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-fuchsia-400 focus:outline-none ${
          error ? "border-red-400 focus:border-red-500" : ""
        }`}
      />
      <datalist id={`${label}-options`}>
        {filtered.map((s) => (
          <option key={s} value={s} />
        ))}
      </datalist>
    </FormField>
  );
}
