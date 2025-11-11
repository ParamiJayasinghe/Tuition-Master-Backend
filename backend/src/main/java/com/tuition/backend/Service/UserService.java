package com.tuition.backend.Service;

import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(UserDto userDTO) {
        // Check for duplicates
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user entity
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole().toUpperCase() : null);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by id
    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Update user
    public User updateUser(Long id, UserDto userDTO) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // If username changed, ensure uniqueness
        String newUsername = userDTO.getUsername();
        if (newUsername != null && !newUsername.equals(existing.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new RuntimeException("Username already exists");
            }
            existing.setUsername(newUsername);
        }

        // If email changed, ensure uniqueness
        String newEmail = userDTO.getEmail();
        if (newEmail != null && !newEmail.equals(existing.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already exists");
            }
            existing.setEmail(newEmail);
        }

        // Update password if provided (remember to encode later)
        if (userDTO.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Update role if provided
        if (userDTO.getRole() != null) {
            existing.setRole(userDTO.getRole().toUpperCase());
        }

        // Optionally update isActive via DTO (if you add it). For now we leave it as-is.

        return userRepository.save(existing);
    }

    // Delete user (hard delete)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}

