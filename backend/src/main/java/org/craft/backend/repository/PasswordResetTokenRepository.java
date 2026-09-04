package org.craft.backend.repository;

import org.craft.backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    Optional<PasswordResetToken> findByUserId(UUID userId);

    @Query("SELECT t.user.id FROM PasswordResetToken t WHERE t.token = :token")
    Optional<UUID> findUserIdByToken(@Param("token") String token);

    void deleteByToken(String token);
}
