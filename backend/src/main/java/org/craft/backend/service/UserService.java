package org.craft.backend.service;

import lombok.Data;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Data
public class UserService {
    private final UserRepository userRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    public User getUserByName(String name) {
        return userRepository.findByName(name).orElseThrow(() -> new UserNotFoundException("User " + name + " not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with the email " +
                "address " + email + " was not found"));
    }

    public void delete(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.deleteById(id);
    }

    //TODO: create (with a CreateRequest dto as an argument)
    //TODO: rename, change email, change password methods
    //TODO: generic editUser method for a future profile page
    //TODO: user profile pictures, including adding, changing and maybe removing
    //TODO: real auth eventually. for now just seed one user and scope everything to that one
}
