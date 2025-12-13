package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.exception.ConflictException;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.core.port.CommentRepositoryPort;
import com.example.guestbook.core.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private static final Duration DELETE_WINDOW = Duration.ofHours(24);

    private final CommentRepositoryPort commentRepository;
    private final CatalogRepositoryPort catalogRepository;
    private final UserRepositoryPort userRepository;
    private final Clock clock;

    public CommentService(CommentRepositoryPort commentRepository,
                          CatalogRepositoryPort catalogRepository,
                          UserRepositoryPort userRepository) {
        this.commentRepository = commentRepository;
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
        this.clock = Clock.systemUTC();
    }

    @Transactional(readOnly = true)
    public Page<Comment> getComments(long bookId, PageRequest pageRequest) {
        if (!catalogRepository.existsById(bookId)) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        return commentRepository.findByBookId(bookId, pageRequest);
    }

    public Comment addComment(long bookId, String author, String text) {
        Map<String, String> errors = new HashMap<>();
        if (bookId <= 0) {
            errors.put("bookId", "must be positive");
        }
        if (author == null || author.isBlank() || author.length() > 64) {
            errors.put("author", "required, up to 64 characters");
        }
        if (text == null || text.isBlank() || text.length() > 1000) {
            errors.put("text", "required, up to 1000 characters");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Comment validation failed", errors);
        }

        if (!catalogRepository.existsById(bookId)) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        Comment toSave = new Comment(0, bookId, author.trim(), text.trim(), Instant.now(clock), null);
        Comment saved = commentRepository.save(toSave);
        log.info("Comment created: id={}, bookId={}, author={}", saved.id(), saved.bookId(), saved.author());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByUser(long userId, PageRequest pageRequest) {
        if (userId <= 0) {
            throw new ValidationException("Invalid userId", Map.of("userId", "must be positive"));
        }
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return commentRepository.findByUserId(userId, pageRequest);
    }

    public void deleteComment(long commentId) {
        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        Instant now = Instant.now(clock);
        Duration sinceCreation = Duration.between(existing.createdAt(), now);
        if (sinceCreation.compareTo(DELETE_WINDOW) > 0) {
            throw new ConflictException("Comment can be deleted only within 24 hours after creation");
        }

        if (commentRepository.deleteById(commentId)) {
            log.info("Comment deleted: id={}, bookId={}", commentId, existing.bookId());
        } else {
            throw new NotFoundException("Failed to delete comment: " + commentId);
        }
    }
}
