package com.example.guestbook.persistence.jpa.repo;

import com.example.guestbook.persistence.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
