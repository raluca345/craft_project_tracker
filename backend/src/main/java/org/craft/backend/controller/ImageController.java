package org.craft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.ImageUploadResponse;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService;
import org.craft.backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final UserService userService;

    @PostMapping("/me/images")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getFirst();
        return ResponseEntity.ok(imageService.upload(user, file));
    }

    @GetMapping("/images/{*key}")
    public ResponseEntity<byte[]> getImage(@PathVariable String key) throws IOException {
        ImageService.ImageData image = imageService.getImage(key.replaceFirst("^/", ""));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.content());
    }
}
