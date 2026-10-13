import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import { Toaster } from "sonner";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
    {/* Sibling of <App>, so toasts survive route changes and dialog unmounts. */}
    <Toaster
      position="bottom-center"
      toastOptions={{
        classNames: {
          toast: "!rounded-xl !shadow-lg",
          description: "!text-slate-500",
          success: "!border-emerald-200 !bg-emerald-50 !text-emerald-800",
          warning: "!border-amber-200 !bg-amber-50 !text-amber-800",
          info: "!border-sky-200 !bg-sky-50 !text-sky-800",
          error: "!border-red-200 !bg-red-50 !text-red-800",
        },
      }}
    />
  </StrictMode>,
);
