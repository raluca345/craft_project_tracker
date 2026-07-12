package org.craft.backend.repository;

import org.craft.backend.enums.Status;
import org.craft.backend.model.Project;
import org.craft.backend.model.Tag;
import org.craft.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByUser(User user);
    List<Project> findByUserAndPatternNameContainingIgnoreCase(User user, String name);
    List<Project> findByUserAndStatus(User user, Status status);

    @Query("SELECT p FROM Project p JOIN p.tags t WHERE p.user = :user AND t = :tag")
    List<Project> findByUserAndTag(@Param("user") User user, @Param("tag") Tag tag);

    @Query("SELECT p FROM Project p JOIN p.tags t WHERE p.user = :user AND t.name = :tagName")
    List<Project> findByUserAndTagName(@Param("user") User user, @Param("tagName") String tagName);

    @Query("SELECT p FROM Project p JOIN p.tags t " +
            "WHERE p.user = :user " +
            "AND (:nameQuery = '' OR LOWER(p.patternName) LIKE LOWER(CONCAT('%', :nameQuery, '%'))) " +
            "AND t.name IN :tagNames " +
            "GROUP BY p " +
            "HAVING COUNT(DISTINCT t.name) = :tagCount")
    List<Project> searchByUserAndNameAndAllTags(
            @Param("user") User user,
            @Param("nameQuery") String nameQuery,
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") long tagCount
    );
}
