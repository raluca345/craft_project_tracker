import { useState } from "react";
import { FaEye, FaEyeSlash } from "react-icons/fa6";
import FormField from "./FormField.jsx";

const INPUT_STYLE =
  "w-full rounded-lg border border-slate-300 px-3 py-2 pr-10 text-sm focus:border-fuchsia-400 focus:outline-none";

const INPUT_ERROR_STYLE =
  "w-full rounded-lg border border-red-400 px-3 py-2 pr-10 text-sm focus:border-red-500 focus:outline-none";

export default function PasswordField({
  label,
  id,
  value,
  onChange,
  autoComplete,
  required,
  minLength,
  error,
}) {
  const [visible, setVisible] = useState(false);

  return (
    <FormField label={label} error={error}>
      <span className="relative">
        <input
          type={visible ? "text" : "password"}
          id={id}
          autoComplete={autoComplete}
          className={error ? INPUT_ERROR_STYLE : INPUT_STYLE}
          value={value}
          onChange={onChange}
          required={required}
          minLength={minLength}
        />
        <button
          type="button"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? "Hide password" : "Show password"}
          aria-pressed={visible}
          className="absolute top-1/2 right-2.5 -translate-y-1/2 text-slate-400 hover:text-fuchsia-500"
        >
          {visible ? (
            <FaEyeSlash className="h-4 w-4" />
          ) : (
            <FaEye className="h-4 w-4" />
          )}
        </button>
      </span>
    </FormField>
  );
}
