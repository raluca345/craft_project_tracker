import { useCallback, useState } from "react";
import { getErrorMessage } from "../commons/errors";

export function useProjectError() {
  const [error, setError] = useState(null);

  const showError = useCallback((err) => {
    console.error(err);
    setError(getErrorMessage(err));
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    error,
    showError,
    clearError,
  };
}
