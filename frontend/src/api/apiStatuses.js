import { handleResponse, API_ROOT } from "./apiCore";

export async function getStatuses() {
  const res = await fetch(`${API_ROOT}/me/statuses`);
  return handleResponse(res);
}

const STATUS_LABELS = {
  TO_DO: "Wishlist",
  IN_PROGRESS: "On the Needles",
  ASSEMBLING: "Assembling",
  FINISHED: "Finished",
};

export function formatStatus(status) {
  return STATUS_LABELS[status] ?? status;
}
