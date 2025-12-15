package com.example.guestbook.core.port;

import com.example.guestbook.core.domain.ConfirmationToken;

import java.util.Optional;

public interface ConfirmationTokenRepositoryPort {
    ConfirmationToken save(ConfirmationToken token);

    Optional<ConfirmationToken> findByToken(String token);
}
