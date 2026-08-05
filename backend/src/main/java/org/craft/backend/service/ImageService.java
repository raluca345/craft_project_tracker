package org.craft.backend.service;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.ImageUploadResponse;
import org.craft.backend.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final R2Service r2Service;

    public ImageUploadResponse upload(User user, MultipartFile file) throws IOException {
        String key = user.getId() + "/" + UUID.randomUUID() + ".png";

        r2Service.putObject(key, file.getBytes(), "image/png");

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .toUriString() + "/api/v1/images/" + key;

        return new ImageUploadResponse(key, url);
    }

    public ImageData getImage(String key) throws IOException {
        try (var object = r2Service.getObject(key)) {
            return new ImageData(object.response().contentType(), object.readAllBytes());
        }
    }

    public record ImageData(String contentType, byte[] content) {
    }
}
