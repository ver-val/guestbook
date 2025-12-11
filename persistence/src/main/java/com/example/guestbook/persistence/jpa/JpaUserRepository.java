package com.example.guestbook.persistence.jpa;

import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.port.UserRepositoryPort;
import com.example.guestbook.persistence.jpa.entity.UserEntity;
import com.example.guestbook.persistence.jpa.repo.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public class JpaUserRepository implements UserRepositoryPort {

    private final UserRepository userRepository;

    public JpaUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(long id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole()
        );
    }
}
