package com.example.guestbook.core.port;

import com.example.guestbook.core.domain.User;

import java.util.Optional;
import java.util.List;

public interface UserRepositoryPort {
    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    User save(User user);

    List<User> findAll();
}
