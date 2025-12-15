package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.port.UserRepositoryPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepositoryPort userRepository;

    public UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    public java.util.Optional<User> findOptionalByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public java.util.Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return java.util.Optional.empty();
        }
        return findOptionalByUsername(auth.getName());
    }

    public java.util.List<User> listActiveNonAdminUsers() {
        return userRepository.findAll().stream()
                .filter(User::enabled)
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.role()))
                .toList();
    }

    @Transactional
    public User register(String username, String email, String encodedPassword, String role) {
        Map<String, String> errors = new HashMap<>();
        if (username == null || username.isBlank() || username.length() > 128) {
            errors.put("username", "required, up to 128 characters");
        }
        if (email == null || email.isBlank() || email.length() > 255) {
            errors.put("email", "required, up to 255 characters");
        }
        if (role == null || role.isBlank()) {
            errors.put("role", "required");
        }
        if (encodedPassword == null || encodedPassword.isBlank()) {
            errors.put("password", "required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("User validation failed", errors);
        }

        userRepository.findByUsername(username).ifPresent(u -> {
            throw new ValidationException("User already exists", Map.of("username", "taken"));
        });

        User toSave = new User(0, username.trim(), encodedPassword, role.trim(), email.trim(), false);
        return userRepository.save(toSave);
    }

    @Transactional
    public User enableUser(long userId) {
        User existing = getUser(userId);
        User updated = new User(existing.id(), existing.username(), existing.password(), existing.role(), existing.email(), true);
        return userRepository.save(updated);
    }
}
