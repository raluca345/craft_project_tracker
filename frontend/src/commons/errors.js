import { ApiError } from "../api/apiCore";

const FALLBACKS = {
  400: "There was a problem with your request. Please check your input and try again.",
  401: "Your session has expired. Please sign in again.",
  403: "You don't have permission to do that.",
  404: "The item you're looking for was not found. It may have been deleted.",
  409: "This email is already in use. Please use a different one.",
  410: "This link has expired. Please request a new one.",
  413: "That file is too large. Please choose a smaller image.",
  503: "We couldn't save your image right now. Please try again in a moment.",
  500: "Something went wrong. Please try again.",
};

function isDisplayable(message) {
  if (!message || typeof message !== "string") return false;
  const trimmed = message.trim();
  if (!trimmed) return false;
  // Never surface technical fallbacks like Spring's status text or empty bodies.
  return !/^(Bad Request|Unauthorized|Forbidden|Not Found|Conflict|Gone|Internal Server Error)$/i.test(trimmed);
}

// Backend messages are display-safe, so prefer them; fall back to a friendly
// generic per status when the body is empty or technical.
export function getErrorMessage(error) {
  if (error instanceof ApiError) {
    if (isDisplayable(error.message)) return error.message;
    return FALLBACKS[error.status] ?? "Something went wrong. Please try again.";
  }

  return "Something went wrong. Please try again.";
}
