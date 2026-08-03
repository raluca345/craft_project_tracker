import { handleResponse, API_ROOT } from "./apiCore";

export async function autocomplete(query) {
  const params = new URLSearchParams();
  if (query) params.set("query", query);
  const q = params.toString();
  const res = await fetch(`${API_ROOT}/tags/search?${q}`);

  return handleResponse(res);
}
