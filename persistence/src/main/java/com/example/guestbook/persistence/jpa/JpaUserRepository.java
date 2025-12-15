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

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        if (user.id() > 0) {
            entity.setId(user.id());
        }
        entity.setUsername(user.username());
        entity.setPassword(user.password());
        entity.setRole(user.role());
        entity.setEmail(user.email());
        entity.setEnabled(user.enabled());
        return toDomain(userRepository.save(entity));
    }

    @Override
    public java.util.List<User> findAll() {
        return userRepository.findAll().stream().map(this::toDomain).toList();
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getEmail(),
                entity.isEnabled()
        );
    }
}
