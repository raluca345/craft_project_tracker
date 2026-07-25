package org.craft.backend.dto;

import lombok.Data;
import org.craft.backend.model.Tag;

import java.util.List;
import java.util.UUID;

@Data
public class TagResponse {
    private UUID id;
    private String name;

    public static TagResponse toResponse(Tag tag) {
        TagResponse tagResponse = new TagResponse();
        tagResponse.setId(tag.getId());
        tagResponse.setName(tag.getName());
        return tagResponse;
    }

    public static List<TagResponse> toResponses(List<Tag> tags) {
        return tags.stream().map(TagResponse::toResponse).toList();
    }
}
