export const API_BASE = "http://localhost:8080/api/v1/me/projects";
export const API_ROOT = "http://localhost:8080/api/v1";

const TOKEN_KEY = "auth_token";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function handleResponse(res) {
  if (!res.ok) {
    let message;
    try {
      const body = await res.json();
      message = body.error ?? body.message ?? res.statusText;
    } catch {
      message = res.statusText;
    }
    throw new ApiError(message, res.status);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}
