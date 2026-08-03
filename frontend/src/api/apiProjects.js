import { handleResponse, API_BASE } from "./apiCore";

export async function getProjects() {
  const res = await fetch(API_BASE);
  return handleResponse(res);
}

export async function createProject(project) {
  const res = await fetch(API_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(project),
  });
  return handleResponse(res);
}

export async function editProject(id, project) {
  const res = await fetch(`${API_BASE}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(project),
  });
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
