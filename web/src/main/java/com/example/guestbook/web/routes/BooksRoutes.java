package com.example.guestbook.web.routes;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BooksRoutes {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "author");
    private static final Set<String> COMMENT_SORT_FIELDS = Set.of("created_at");

    private final CatalogService catalogService;
    private final CommentService commentService;

    public BooksRoutes(CatalogService catalogService, CommentService commentService) {
        this.catalogService = catalogService;
        this.commentService = commentService;
    }

    public void register(Javalin app) {
        app.get("/api/books", this::listBooks);
        app.get("/api/books/{id}", this::bookDetails);
        app.post("/api/books", this::addBook);
        app.post("/api/books/{id}/comments", this::addComment);
    }

    private void listBooks(Context ctx) {
        String q = Optional.ofNullable(ctx.queryParam("q")).orElse("");
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);
        String sortParam = Optional.ofNullable(ctx.queryParam("sort")).orElse("id,asc");
        PageRequest pageRequest = buildPageRequest(page, size, sortParam, ALLOWED_SORT_FIELDS, "id");
        Page<Book> books = catalogService.searchBooks(Optional.ofNullable(q).orElse(""), pageRequest, pageRequest.sort());
        ctx.json(books);
    }

    private void bookDetails(Context ctx) {
        long id = parseLong(ctx.pathParam("id"));
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(5);
        String sortParam = Optional.ofNullable(ctx.queryParam("sort")).orElse("created_at,desc");
        PageRequest commentPage = buildPageRequest(page, size, sortParam, COMMENT_SORT_FIELDS, "created_at");
        Book book = catalogService.getBook(id);
        Page<Comment> comments = commentService.getComments(id, commentPage);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("book", book);
        payload.put("comments", comments);
        ctx.json(payload);
    }

    private void addBook(Context ctx) {
        CreateBookRequest req = ctx.bodyValidator(CreateBookRequest.class)
                .check(r -> r.title != null && !r.title.isBlank() && r.title.length() <= 255, "title required, up to 255 chars")
                .check(r -> r.author != null && !r.author.isBlank() && r.author.length() <= 255, "author required, up to 255 chars")
                .check(r -> r.description == null || r.description.length() <= 2000, "description up to 2000 chars")
                .get();
        Book saved = catalogService.addBook(req.title, req.author, req.description);
        ctx.status(201);
        ctx.header("Location", "/books/" + saved.id());
        ctx.json(saved);
    }

    private void addComment(Context ctx) {
        long bookId = parseLong(ctx.pathParam("id"));
        CreateCommentRequest body = ctx.bodyValidator(CreateCommentRequest.class)
                .check(r -> r.author != null && !r.author.isBlank() && r.author.length() <= 64, "author required, up to 64 chars")
                .check(r -> r.text != null && !r.text.isBlank() && r.text.length() <= 1000, "text required, up to 1000 chars")
                .get();
        Comment saved = commentService.addComment(bookId, body.author, body.text);
        ctx.status(201);
        ctx.header("Location", "/books/" + bookId + "/comments/" + saved.id());
        ctx.json(saved);
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

    private long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid id", Map.of("id", "must be a number"));
        }
    }

    private record CreateBookRequest(String title, String author, String description) {
    }

    private record CreateCommentRequest(String author, String text) {
    }
}
