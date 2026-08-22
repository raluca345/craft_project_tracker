package org.craft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.craft.backend.config.AuthHelper;
import org.craft.backend.dto.ImageUploadResponse;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ImageController {
    private final ImageService imageService;
    private final AuthHelper authHelper;

    @PostMapping("/me/images")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                      HttpServletRequest request) throws IOException {
        User user = authHelper.getCurrentUser();
        ImageService.UploadedImage uploaded = imageService.upload(user, file);

        String url = ServletUriComponentsBuilder.fromContextPath(request)
                .path("/api/v1/images/")
                .path(uploaded.key())
                .toUriString();

        return ResponseEntity.ok(new ImageUploadResponse(uploaded.key(), url));
    }

    @GetMapping("/images/{*key}")
    public ResponseEntity<byte[]> getImage(@PathVariable String key) throws IOException {
        ImageService.ImageData image = imageService.getImage(key.replaceFirst("^/", ""));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .body(image.content());
    }
}
