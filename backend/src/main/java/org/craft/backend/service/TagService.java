package org.craft.backend.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.craft.backend.model.Tag;
import org.craft.backend.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Tag createTag(String name) {
        Tag tag = new Tag(name);
        return tagRepository.save(tag);
    }

    public Optional<Tag> getByName(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }

    public Tag getByNameOrCreate(String name) {
        return tagRepository.findByNameIgnoreCase(name).orElseGet(() -> createTag(name));
    }

    public List<Tag> autocomplete(String query) {
        return tagRepository.findByNameContainingIgnoreCase(query);
    }

}
