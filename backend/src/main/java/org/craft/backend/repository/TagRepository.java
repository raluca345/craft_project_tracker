package org.craft.backend.repository;

import org.craft.backend.model.Tag;
import org.craft.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByNameContainingIgnoreCase(@Param("search") String search);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.projects p WHERE p.user = :user")
    List<Tag> findByUser(@Param("user") User user);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.projects p WHERE p.user = :user AND LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Tag> findByUserAndNameContainingIgnoreCase(@Param("user") User user, @Param("search") String search);

    Optional<Tag> findByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.projects p WHERE p.user = :user AND LOWER(t.name) = LOWER(:name)")
    Optional<Tag> findByUserAndNameIgnoreCase(@Param("user") User user, @Param("name") String name);
}
