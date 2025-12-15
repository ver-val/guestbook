package com.example.guestbook.persistence.jpa;

import com.example.guestbook.core.domain.ConfirmationToken;
import com.example.guestbook.core.port.ConfirmationTokenRepositoryPort;
import com.example.guestbook.persistence.jpa.entity.ConfirmationTokenEntity;
import com.example.guestbook.persistence.jpa.entity.UserEntity;
import com.example.guestbook.persistence.jpa.repo.ConfirmationTokenRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public class JpaConfirmationTokenRepository implements ConfirmationTokenRepositoryPort {

    private final ConfirmationTokenRepository repo;

    public JpaConfirmationTokenRepository(ConfirmationTokenRepository repo) {
        this.repo = repo;
    }

    @Override
    public ConfirmationToken save(ConfirmationToken token) {
        ConfirmationTokenEntity entity = toEntity(token);
        ConfirmationTokenEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ConfirmationToken> findByToken(String token) {
        return repo.findByToken(token).map(this::toDomain);
    }

    private ConfirmationTokenEntity toEntity(ConfirmationToken token) {
        ConfirmationTokenEntity e = new ConfirmationTokenEntity();
        if (token.id() > 0) {
            e.setId(token.id());
        }
        e.setToken(token.token());
        e.setCreatedAt(token.createdAt());
        e.setExpiresAt(token.expiresAt());
        e.setUsed(token.used());
        UserEntity u = new UserEntity();
        u.setId(token.userId());
        e.setUser(u);
        return e;
    }

    private ConfirmationToken toDomain(ConfirmationTokenEntity e) {
        return new ConfirmationToken(
                e.getId(),
                e.getToken(),
                e.getUser() != null ? e.getUser().getId() : 0,
                e.getCreatedAt(),
                e.getExpiresAt(),
                e.isUsed()
        );
    }
}
