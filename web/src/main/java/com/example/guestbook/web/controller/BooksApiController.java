package com.example.guestbook.web.controller;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/books")
public class BooksApiController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "author");
    private static final Set<String> COMMENT_SORT_FIELDS = Set.of("created_at");

    private final CatalogService catalogService;
    private final CommentService commentService;

    public BooksApiController(CatalogService catalogService, CommentService commentService) {
        this.catalogService = catalogService;
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Page<Book>> listBooks(@RequestParam(required = false) String q,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "id,asc") String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort, ALLOWED_SORT_FIELDS, "id");
        Page<Book> books = catalogService.searchBooks(Optional.ofNullable(q).orElse(""), pageRequest, pageRequest.sort());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> bookDetails(@PathVariable long id,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size,
                                                           @RequestParam(defaultValue = "created_at,desc") String sort) {
        PageRequest commentPage = buildPageRequest(page, size, sort, COMMENT_SORT_FIELDS, "created_at");
        Book book = catalogService.getBook(id);
        Page<Comment> comments = commentService.getComments(id, commentPage);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("book", book);
        payload.put("comments", comments);
        return ResponseEntity.ok(payload);
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody CreateBookRequest request) {
        Book saved = catalogService.addBook(request.title(), request.author(), request.description(), request.pubYear());
        return ResponseEntity.created(URI.create("/api/books/" + saved.id())).body(saved);
    }

    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Comment> createCommentForBook(@PathVariable long id,
                                                        @Valid @RequestBody CreateCommentBody body) {
        Comment saved = commentService.addComment(id, body.author(), body.text());
        return ResponseEntity
                .created(URI.create("/api/books/" + id + "/comments/" + saved.id()))
                .body(saved);
    }

    @PostMapping("/comments")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CreateCommentRequest request) {
        Comment saved = commentService.addComment(request.bookId(), request.author(), request.text());
        return ResponseEntity
                .created(URI.create("/api/books/" + request.bookId() + "/comments/" + saved.id()))
                .body(saved);
    }

    private PageRequest buildPageRequest(int page, int size, String sortParam, Set<String> allowedFields, String defaultField) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (page < 0) {
            errors.put("page", "must be non-negative");
        }
        if (size <= 0) {
            errors.put("size", "must be positive");
        }
        Sort sort = parseSort(sortParam, allowedFields, defaultField, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Pagination parameters are invalid", errors);
        }
        return new PageRequest(page, size, sort);
    }

    private Sort parseSort(String raw, Set<String> allowedFields, String defaultField, Map<String, String> errors) {
        if (raw == null || raw.isBlank()) {
            return Sort.by(defaultField, Sort.Direction.ASC);
        }
        String[] parts = raw.split(",");
        String field = parts[0].trim();
        if (!allowedFields.contains(field)) {
            errors.put("sort", "unsupported field: " + field);
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            try {
                direction = Sort.parseDirection(parts[1]);
            } catch (IllegalArgumentException e) {
                errors.put("sort", "unsupported direction: " + parts[1]);
            }
        }
        return Sort.by(field, direction);
    }

    private record CreateCommentRequest(
            @Positive(message = "bookId must be positive") long bookId,
            @NotBlank @Size(max = 64) String author,
            @NotBlank @Size(max = 1000) String text
    ) {
    }

    private record CreateCommentBody(
            @NotBlank @Size(max = 64) String author,
            @NotBlank @Size(max = 1000) String text
    ) {
    }

    private record CreateBookRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 255) String author,
            @Size(max = 2000) String description,
            @Min(1) @Max(2100) Integer pubYear
    ) {
    }
}
