export const API_ROOT =
  import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";
export const API_BASE = `${API_ROOT}/me/projects`;

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
    if (res.status === 403) {
      clearToken();
      window.location.href = "/login";
      throw new ApiError("Session expired", 403);
    }
    let message;
    try {
      const body = await res.json();
      message = body.message ?? body.error ?? res.statusText;
    } catch {
      message = res.statusText;
    }
    throw new ApiError(message, res.status);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}
