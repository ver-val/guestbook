package com.example.guestbook.persistence.jpa.repo;

import com.example.guestbook.persistence.jpa.entity.ConfirmationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationTokenEntity, Long> {
    Optional<ConfirmationTokenEntity> findByToken(String token);
}
