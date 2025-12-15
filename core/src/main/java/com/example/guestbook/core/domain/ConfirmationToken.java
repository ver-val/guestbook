package com.example.guestbook.core.domain;

import java.time.Instant;

public record ConfirmationToken(
        long id,
        String token,
        long userId,
        Instant createdAt,
        Instant expiresAt,
        boolean used
) {
}
