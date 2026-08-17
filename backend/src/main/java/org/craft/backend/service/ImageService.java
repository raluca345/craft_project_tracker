package org.craft.backend.service;

import lombok.RequiredArgsConstructor;
import org.craft.backend.exceptions.ImageFileException;
import org.craft.backend.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024; // 10MB, must match the multipart limits in application.properties

    // file extension (normalized to lowercase, no dot) -> content type to store & serve
    private static final Map<String, String> SUPPORTED_IMAGE_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "webp", "image/webp"
    );

    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private final R2Service r2Service;

    public UploadedImage upload(User user, MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String contentType = SUPPORTED_IMAGE_TYPES.get(extension);
        if (contentType == null) {
            throw new ImageFileException(
                    "Unsupported file type. Only PNG, JPEG and WebP images are accepted.");
        }

        if (!SUPPORTED_MIME_TYPES.contains(file.getContentType())) {
            throw new ImageFileException(
                    "Unsupported file type. Only PNG, JPEG and WebP images are accepted.");
        }

        if (file.getSize() <= 0) {
            throw new ImageFileException("The image file is empty.");
        }

        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new ImageFileException(
                    "File is too large. Maximum size is " + (MAX_UPLOAD_BYTES / (1024 * 1024)) + "MB.");
        }

        // The MIME type can lie or be empty; verify the leading bytes match the
        // declared format (catches renames without needing an image decoder).
        if (!matchesSignature(file.getInputStream(), extension)) {
            throw new ImageFileException(
                    "The file doesn't look like a valid image. Only PNG, JPEG and WebP images are accepted.");
        }

        String key = user.getId() + "/" + UUID.randomUUID() + "." + extension;

        r2Service.putObject(key, file.getBytes(), contentType);

        return new UploadedImage(key, contentType);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    private boolean matchesSignature(InputStream in, String extension) throws IOException {
        // Read just enough header bytes to identify the format. We intentionally
        // don't close the stream here — it's the multipart-provided stream and
        // closing it can invalidate the source for the later full read.
        byte[] head = in.readNBytes(12);

        if ("png".equals(extension) && head.length >= 8) {
            byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            for (int i = 0; i < pngSignature.length; i++) {
                if (head[i] != pngSignature[i]) return false;
            }
            return true;
        }

        if (("jpg".equals(extension) || "jpeg".equals(extension)) && head.length >= 3) {
            return (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
        }

        if ("webp".equals(extension) && head.length >= 12) {
            boolean riff = head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F';
            boolean webp = head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
            return riff && webp;
        }

        return false;
    }

    public ImageData getImage(String key) throws IOException {
        try (var object = r2Service.getObject(key)) {
            return new ImageData(object.response().contentType(), object.readAllBytes());
        }
    }

    public record ImageData(String contentType, byte[] content) {
    }

    public record UploadedImage(String key, String contentType) {
    }
}
