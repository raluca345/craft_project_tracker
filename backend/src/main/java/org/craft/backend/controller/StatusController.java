package org.craft.backend.controller;

import org.craft.backend.enums.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/me/statuses")
public class StatusController {

    @GetMapping
    public ResponseEntity<List<String>> getStatuses() {
        List<String> statuses = Stream.of(Status.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(statuses);
    }
}
