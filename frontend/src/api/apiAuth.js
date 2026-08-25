import { API_ROOT, authHeaders, handleResponse } from "./apiCore";

export async function login(email, password) {
  const res = await fetch(`${API_ROOT}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  return handleResponse(res);
}

export async function register(name, email, password) {
  const res = await fetch(`${API_ROOT}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, email, password }),
  });
  return handleResponse(res);
}

export async function getMe() {
  const res = await fetch(`${API_ROOT}/me`, {
    headers: { ...authHeaders() },
  });
  return handleResponse(res);
}

export async function uploadAvatar(file) {
  const form = new FormData();
  form.append("file", file);
  const res = await fetch(`${API_ROOT}/me/avatar`, {
    method: "POST",
    headers: { ...authHeaders() },
    body: form,
  });
  return handleResponse(res);
}

export async function rename(name) {
  const res = await fetch(`${API_ROOT}/me/rename`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ name }),
  });
  return handleResponse(res);
}

export async function changeEmail(email) {
  const res = await fetch(`${API_ROOT}/me/email`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ email }),
  });
  return handleResponse(res);
}
