// Client-side image validation, mirroring the rules enforced by the backend
// (ImageService): PNG / JPEG / WebP, up to 10MB. Validation here is a fail-fast
// convenience — the backend is the source of truth.

export const MAX_IMAGE_UPLOAD_BYTES = 10 * 1024 * 1024;

export const SUPPORTED_IMAGE_EXTENSIONS = ["png", "jpg", "jpeg", "webp"];

export const SUPPORTED_IMAGE_MIME_TYPES = [
  "image/png",
  "image/jpeg",
  "image/webp",
];

export const IMAGE_ACCEPT = SUPPORTED_IMAGE_MIME_TYPES.join(",");

export function validateImageFile(file) {
  if (!file) return null;

  const extension = file.name.split(".").pop()?.toLowerCase() ?? "";
  const isSupportedType =
    SUPPORTED_IMAGE_MIME_TYPES.includes(file.type) &&
    SUPPORTED_IMAGE_EXTENSIONS.includes(extension);

  if (!isSupportedType) {
    return "Only PNG, JPEG and WebP images are supported.";
  }

  if (file.size > MAX_IMAGE_UPLOAD_BYTES) {
    return "Image is too large. Maximum size is 10MB.";
  }

  return null;
}