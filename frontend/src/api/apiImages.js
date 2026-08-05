import { API_ROOT, handleResponse } from "./apiCore";

export async function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch(`${API_ROOT}/me/images`, {
    method: "POST",
    body: formData,
  });

  return handleResponse(res);
}
