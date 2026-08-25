package org.craft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.craft.backend.config.AuthHelper;
import org.craft.backend.dto.ImageUploadResponse;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ImageController {
    private final ImageService imageService;
    private final AuthHelper authHelper;

    @PostMapping("/me/images")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        User user = authHelper.getCurrentUser();
        ImageService.UploadedImage uploaded = imageService.upload(user, file);

        return ResponseEntity.ok(new ImageUploadResponse(uploaded.key(), imageService.presignUrl(uploaded.key())));
    }
}
