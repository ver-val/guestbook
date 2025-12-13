package com.example.guestbook.core.domain;

import java.time.Instant;

public record Comment(
        long id,
        long bookId,
        String author,
        String text,
        Instant createdAt,
        Long userId
) {
}
