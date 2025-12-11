package com.example.guestbook.persistence.jpa.repo;

import com.example.guestbook.persistence.jpa.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    Page<CommentEntity> findByBookId(Long bookId, Pageable pageable);

    List<CommentEntity> findByUserId(Long userId);

    Page<CommentEntity> findByUserId(Long userId, Pageable pageable);
}
