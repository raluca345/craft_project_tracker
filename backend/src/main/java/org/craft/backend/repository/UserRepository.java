package org.craft.backend.repository;

import org.craft.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
}
