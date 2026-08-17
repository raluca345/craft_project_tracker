import { ApiError } from "../api/apiCore";

export function getErrorMessage(error) {
  if (error instanceof ApiError) {
    switch (error.status) {
      case 400:
        return "There was a problem with your request. Please check your input and try again.";
      case 401:
        return "Your session has expired. Please sign in again.";
      case 403:
        return "You don't have permission to do that.";
      case 404:
        return "The item you're looking for was not found. It may have been deleted.";
      case 409:
        return "This item was already changed. Please refresh and try again.";
      case 413:
        return "That file is too large. Please choose a smaller image.";
      case 503:
        return "We couldn't save your image right now. Please try again in a moment.";
      case 500:
        return "Something went wrong. Please try again.";
      default:
        return "Something went wrong. Please try again.";
    }
  }

  return "Something went wrong. Please try again.";
}
