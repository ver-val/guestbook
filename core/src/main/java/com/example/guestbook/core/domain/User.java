package com.example.guestbook.core.domain;

public record User(
        long id,
        String username,
        String password,
        String role
) {
}
