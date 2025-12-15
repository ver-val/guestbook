package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.ConfirmationToken;
import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.port.ConfirmationTokenRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AccountConfirmationService {

    private final ConfirmationTokenRepositoryPort tokenRepository;
    private final UserService userService;
    private final Clock clock;

    public AccountConfirmationService(ConfirmationTokenRepositoryPort tokenRepository, UserService userService) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public ConfirmationToken createToken(User user) {
        Instant now = Instant.now(clock);
        ConfirmationToken token = new ConfirmationToken(
                0,
                UUID.randomUUID().toString().replace("-", ""),
                user.id(),
                now,
                now.plus(Duration.ofHours(24)),
                false
        );
        return tokenRepository.save(token);
    }

    @Transactional
    public User confirm(String tokenValue) {
        ConfirmationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new NotFoundException("Invalid confirmation code"));
        if (token.used()) {
            throw new NotFoundException("Code already used");
        }
        if (token.expiresAt().isBefore(Instant.now(clock))) {
            throw new NotFoundException("Code expired");
        }
        User enabled = userService.enableUser(token.userId());
        ConfirmationToken usedToken = new ConfirmationToken(
                token.id(),
                token.token(),
                token.userId(),
                token.createdAt(),
                token.expiresAt(),
                true
        );
        tokenRepository.save(usedToken);
        return enabled;
    }
}
