package org.craft.backend.repository;

import org.craft.backend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByNameContainingIgnoreCase(@Param("search") String search);

    Optional<Tag> findByNameIgnoreCase(@Param("name") String name);

}
