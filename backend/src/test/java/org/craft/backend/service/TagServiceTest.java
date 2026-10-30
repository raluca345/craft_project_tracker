package org.craft.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.craft.backend.model.Tag;
import org.craft.backend.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void getByNameOrCreate_existingTagReturnsTag() {
        String tagName = "wearable";
        Optional<Tag> tag = Optional.of(new Tag(tagName));
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(tag);

        Tag result = tagService.getByNameOrCreate(tagName);

        assertNotNull(result);
        assertEquals(tagName, result.getName());
        verify(tagRepository, never()).save(any());
    }

    @Test
    void getByNameOrCreate_newTagCreatesAndSaves() {
        String tagName = "wearable";
        Tag newTag = new Tag(tagName);
        when(tagRepository.findByNameIgnoreCase(tagName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

        Tag result = tagService.getByNameOrCreate(tagName);

        assertNotNull(result);
        assertEquals(tagName, result.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void autocomplete_delegatesToRepository() {
        String query = "s";
        List<Tag> tags = List.of(new Tag("socks"), new Tag("sweater"), new Tag("scarf"));
        when(tagRepository.findByNameContainingIgnoreCase(query)).thenReturn(tags);

        List<Tag> result = tagService.autocomplete(query);

        assertEquals(3, result.size());
        assertEquals("socks", result.get(0).getName());
        assertEquals("sweater", result.get(1).getName());
        assertEquals("scarf", result.get(2).getName());
        verify(tagRepository).findByNameContainingIgnoreCase(query);
    }

    @Test
    void autocomplete_noMatchReturnsEmptyList() {
        String query = "xyz";
        when(tagRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of());

        List<Tag> result = tagService.autocomplete(query);

        assertTrue(result.isEmpty());
        verify(tagRepository).findByNameContainingIgnoreCase(query);
    }

    @Test
    void autocomplete_emptyQueryReturnsEmptyList() {
        String query = "";
        when(tagRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of());

        List<Tag> result = tagService.autocomplete(query);

        assertTrue(result.isEmpty());
        verify(tagRepository).findByNameContainingIgnoreCase(query);
    }
}