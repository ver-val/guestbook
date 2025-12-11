package com.example.guestbook.core.port;

import com.example.guestbook.core.domain.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(long id);
}
