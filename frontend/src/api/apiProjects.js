import { handleResponse, API_BASE, authHeaders } from "./apiCore";

export async function getProjects() {
  const res = await fetch(API_BASE, { headers: authHeaders() });
  return handleResponse(res);
}

export async function search(query) {
  const params = new URLSearchParams();
  if (query) params.set("query", query);
  const q = params.toString();
  const res = await fetch(`${API_BASE}/search?${q}`, { headers: authHeaders() });
  return handleResponse(res);
}

export async function createProject(project) {
  const res = await fetch(API_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify(project),
  });
  return handleResponse(res);
}

export async function editProject(id, project) {
  const res = await fetch(`${API_BASE}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify(project),
  });
  return handleResponse(res);
}

export async function updateProjectStatus(id, status) {
  const res = await fetch(`${API_BASE}/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ status }),
  });
  return handleResponse(res);
}

export async function updateProjectNotes(id, notes) {
  const res = await fetch(`${API_BASE}/${id}/notes`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ notes: notes || null }),
  });
  return handleResponse(res);
}

export async function deleteProject(id) {
  const res = await fetch(`${API_BASE}/${id}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json", ...authHeaders() },
  });
  return handleResponse(res);
}
