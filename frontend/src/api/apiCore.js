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
    let message;
    let parsedBody = null;
    try {
      parsedBody = await res.json();
      message = parsedBody.message ?? parsedBody.error ?? res.statusText;
    } catch {
      message = res.statusText;
    }
    if ((res.status === 401 || res.status === 403) && !parsedBody?.message) {
      // No usable JSON body: Spring's default empty 401/403 for a missing or
      // expired JWT. Only these trigger the session-expired redirect.
      clearToken();
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
      throw new ApiError("Your session has expired. Please sign in again.", res.status);
    }
    throw new ApiError(message || res.statusText, res.status);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}
