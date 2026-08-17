import { API_ROOT, handleResponse, authHeaders } from "./apiCore";

export async function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch(`${API_ROOT}/me/images`, {
    method: "POST",
    headers: authHeaders(),
    body: formData,
  });

  return handleResponse(res);
}
