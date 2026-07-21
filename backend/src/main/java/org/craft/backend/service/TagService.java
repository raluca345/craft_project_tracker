package org.craft.backend.service;

import lombok.Data;
import org.craft.backend.repository.TagRepository;
import org.springframework.stereotype.Service;

@Service
@Data
public class TagService {
    private final TagRepository tagRepository;

    //TODO: create, read, update, delete methods
    //TODO: search method. or maybe keep that in project service. debatable
    //TODO: method that handles the autocomplete
    //TODO: method to create one or more tags on project save
}
