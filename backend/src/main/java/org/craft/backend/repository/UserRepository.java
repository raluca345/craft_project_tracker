package org.craft.backend.repository;

import org.craft.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findById(UUID id);
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);

    void deleteById(UUID id);
}
