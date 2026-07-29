import { handleResponse, API_ROOT } from "./apiCore";

export async function getStatuses() {
  const res = await fetch(`${API_ROOT}/statuses`);
  return handleResponse(res);
}

export function formatStatus(status) {
  return (
    status
      //basically does IN_PROGRESS -> In Progress
      .replace(/_/g, " ")
      .toLowerCase()
      .replace(/\b\w/g, (c) => c.toUpperCase())
  );
}
