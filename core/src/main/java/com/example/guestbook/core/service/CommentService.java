package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.exception.BookNotFoundException;
import com.example.guestbook.core.exception.CommentTooOldException;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.exception.ForbiddenCommentContentException;
import com.example.guestbook.core.exception.InvalidCommentDeleteException;
import com.example.guestbook.core.exception.ConflictException;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.core.port.CommentRepositoryPort;
import com.example.guestbook.core.port.UserRepositoryPort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private static final java.util.List<String> FORBIDDEN_WORDS = java.util.List.of("spam", "abuse");

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
        Long userId = resolveCurrentUserId();
        String authorName = author == null ? "" : author.trim();
        if ((authorName.isBlank() && userId == null) || authorName.length() > 64) {
            errors.put("author", "required, up to 64 characters");
        }
        if (text == null || text.isBlank() || text.length() > 1000) {
            errors.put("text", "required, up to 1000 characters");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Comment validation failed", errors);
        }

        ensureContentAllowed(text.trim());

        if (!catalogRepository.existsById(bookId)) {
            throw new NotFoundException("Book not found: " + bookId);
        }

        if (authorName.isBlank() && userId != null) {
            authorName = resolveCurrentUserName().orElse("Anonymous");
        }

        Comment toSave = new Comment(0, bookId, authorName, text.trim(), Instant.now(clock), userId);
        Comment saved = commentRepository.save(toSave);
        log.info("Comment created: id={}, bookId={}, author={}", saved.id(), saved.bookId(), saved.author());
        return saved;
    }

    /**
     * Deletes a comment with strict validation (ids, 24h window, ownership handled elsewhere).
     */
    public void delete(long bookId, long commentId) {
        Map<String, String> errors = new HashMap<>();
        if (bookId <= 0) {
            errors.put("bookId", "must be positive");
        }
        if (commentId <= 0) {
            errors.put("commentId", "must be positive");
        }
        if (!errors.isEmpty()) {
            throw new InvalidCommentDeleteException("Comment delete validation failed", errors);
        }

        if (!catalogRepository.existsById(bookId)) {
            throw new BookNotFoundException("Book not found: " + bookId);
        }

        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));

        if (existing.bookId() != bookId) {
            throw new ConflictException("Comment does not belong to book " + bookId);
        }

        Instant now = Instant.now(clock);
        Duration sinceCreation = Duration.between(existing.createdAt(), now);
        if (sinceCreation.compareTo(DELETE_WINDOW) > 0) {
            throw new CommentTooOldException("Comment can be deleted only within 24 hours after creation");
        }

        if (commentRepository.deleteById(commentId)) {
            log.info("Comment deleted: id={}, bookId={}", commentId, bookId);
        } else {
            throw new NotFoundException("Failed to delete comment: " + commentId);
        }
    }

    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByUsername(auth.getName())
                .map(User::id)
                .orElse(null);
    }

    private java.util.Optional<String> resolveCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(auth.getName());
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public void ensureContentAllowed(String text) {
        String normalized = text.toLowerCase();
        boolean forbidden = FORBIDDEN_WORDS.stream().anyMatch(normalized::contains);
        if (forbidden) {
            throw new ForbiddenCommentContentException("Comment contains forbidden content");
        }
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

        boolean isAdmin = isCurrentUserAdmin();
        Long currentUserId = resolveCurrentUserId();
        if (!isAdmin) {
            if (existing.userId() == null || currentUserId == null || !existing.userId().equals(currentUserId)) {
                throw new ConflictException("You can delete only your own comments");
            }
        }
        // Reuse strict validation method
        delete(existing.bookId(), existing.id());
    }
}
