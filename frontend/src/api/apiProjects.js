import { handleResponse, API_BASE } from "./apiCore";

export async function getProjects() {
  const res = await fetch(API_BASE);
  return handleResponse(res);
}

export async function updateProjectStatus(id, status) {
  const res = await fetch(`${API_BASE}/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  return handleResponse(res);
}
