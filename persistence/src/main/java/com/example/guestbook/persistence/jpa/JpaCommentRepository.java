package com.example.guestbook.persistence.jpa;

import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.port.CommentRepositoryPort;
import com.example.guestbook.persistence.jpa.entity.BookEntity;
import com.example.guestbook.persistence.jpa.entity.CommentEntity;
import com.example.guestbook.persistence.jpa.entity.UserEntity;
import com.example.guestbook.persistence.jpa.repo.BookRepository;
import com.example.guestbook.persistence.jpa.repo.CommentRepository;
import com.example.guestbook.persistence.jpa.repo.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@Primary
public class JpaCommentRepository implements CommentRepositoryPort {

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public JpaCommentRepository(CommentRepository commentRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Comment> findByBookId(long bookId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());
        org.springframework.data.domain.Page<CommentEntity> page = commentRepository.findByBookId(bookId, pageable);

        return new Page<>(
                page.map(this::toDomain).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public Comment save(Comment comment) {
        BookEntity book = bookRepository.findById(comment.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + comment.bookId()));
        UserEntity user = null;
        if (comment.userId() != null) {
            user = userRepository.findById(comment.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + comment.userId()));
        }

        CommentEntity entity = new CommentEntity();
        if (comment.id() > 0) {
            entity.setId(comment.id());
        }
        entity.setAuthor(comment.author());
        entity.setText(comment.text());
        entity.setCreatedAt(comment.createdAt() == null ? Instant.now() : comment.createdAt());
        entity.setBook(book);
        entity.setUser(user);

        CommentEntity saved = commentRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(long id) {
        return commentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean deleteById(long id) {
        if (!commentRepository.existsById(id)) {
            return false;
        }
        commentRepository.deleteById(id);
        return true;
    }

    @Override
    public Page<Comment> findByUserId(long userId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());
        org.springframework.data.domain.Page<CommentEntity> page = commentRepository.findByUserId(userId, pageable);
        return new com.example.guestbook.core.domain.Page<>(
                page.map(this::toDomain).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private Comment toDomain(CommentEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getBook() != null ? entity.getBook().getId() : 0,
                entity.getAuthor(),
                entity.getText(),
                entity.getCreatedAt(),
                entity.getUser() != null ? entity.getUser().getId() : null
        );
    }
}
