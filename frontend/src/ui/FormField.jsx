import React from "react";

export default function FormField({ label, error, children }) {
  return (
    <span className="flex flex-col gap-1">
      <label className="text-sm text-slate-600">{label}</label>
      {children}
      {error && <span className="text-xs text-red-500">{error}</span>}
    </span>
  );
}
