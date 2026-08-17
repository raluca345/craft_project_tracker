package org.craft.backend.repository;

import org.craft.backend.model.Project;
import org.craft.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByUser(User user);
    Optional<Project> findByIdAndUser(UUID id,  User user);
    List<Project> findByUserAndPatternNameContainingIgnoreCase(User user, String name);

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
