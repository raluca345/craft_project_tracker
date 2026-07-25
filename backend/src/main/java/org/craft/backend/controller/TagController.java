package org.craft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.TagResponse;
import org.craft.backend.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> autocomplete(@RequestParam String query) {
        List<TagResponse> tags = TagResponse.toResponses(tagService.autocomplete(query));
        return ResponseEntity.ok(tags);
    }
}
