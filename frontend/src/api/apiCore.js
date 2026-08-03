export const API_BASE = "http://localhost:8080/api/v1/me/projects";
export const API_ROOT = "http://localhost:8080/api/v1";

export async function handleResponse(res) {
  if (!res.ok) {
    let message;
    try {
      const body = await res.json();
      message = body.error ?? body.message ?? res.statusText;
    } catch {
      message = res.statusText;
    }
    throw new Error(message);
  }
  return res.json();
}
