package com.example.guestbook.web.http;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BookServlet extends BaseServlet {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "author");
    private final CatalogService catalogService;
    private final CommentService commentService;

    public BookServlet(CatalogService catalogService, CommentService commentService, ObjectMapper objectMapper) {
        super(objectMapper);
        this.catalogService = catalogService;
        this.commentService = commentService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String path = Optional.ofNullable(req.getPathInfo()).orElse("/");
            if ("/".equals(path) || path.isBlank()) {
                if (acceptsHtml(req)) {
                    renderBooksPage(req, resp);
                } else {
                    listBooks(req, resp);
                }
                return;
            }

            PathParts pathParts = parsePath(path);
            if (pathParts.comments) {
                listComments(req, resp, pathParts.bookId);
            } else {
                if (acceptsHtml(req)) {
                    renderBookDetails(req, resp, pathParts.bookId);
                } else {
                    bookDetails(req, resp, pathParts.bookId);
                }
            }
        } catch (Exception ex) {
            handleException(req, resp, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String path = Optional.ofNullable(req.getPathInfo()).orElse("/");
            PathParts pathParts = parsePath(path);
            if (!pathParts.comments) {
                throw new IllegalArgumentException("Comments path expected");
            }
            createComment(req, resp, pathParts.bookId);
        } catch (Exception ex) {
            handleException(req, resp, ex);
        }
    }

    private void listBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = Optional.ofNullable(req.getParameter("q")).orElse("");
        PageRequest pageRequest = parsePageRequest(req, ALLOWED_SORT_FIELDS, "page", "size", 0, 10, "id");
        Sort sort = pageRequest.sort();

        Page<Book> books = catalogService.searchBooks(q, pageRequest, sort);
        writeJson(resp, HttpServletResponse.SC_OK, books);
    }

    private void renderBooksPage(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String q = Optional.ofNullable(req.getParameter("q")).orElse("");
        PageRequest pageRequest = new PageRequest(0, 50, Sort.by("id", Sort.Direction.ASC));
        Page<Book> books = catalogService.searchBooks(q, pageRequest, pageRequest.sort());
        req.setAttribute("books", books.content());
        req.getRequestDispatcher("/WEB-INF/views/books.jsp").forward(req, resp);
    }

    private void bookDetails(HttpServletRequest req, HttpServletResponse resp, long bookId) throws IOException {
        PageRequest commentPageRequest = parsePageRequest(req, Set.of("created_at"), "page", "size", 0, 5, "created_at");

        Book book = catalogService.getBook(bookId);
        Page<Comment> comments = commentService.getComments(bookId, commentPageRequest);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("book", book);
        payload.put("comments", comments);

        writeJson(resp, HttpServletResponse.SC_OK, payload);
    }

    private void renderBookDetails(HttpServletRequest req, HttpServletResponse resp, long bookId) throws Exception {
        Book book = catalogService.getBook(bookId);
        PageRequest commentPageRequest = new PageRequest(0, 100, Sort.by("created_at", Sort.Direction.ASC));
        Page<Comment> comments = commentService.getComments(bookId, commentPageRequest);

        req.setAttribute("book", book);
        req.setAttribute("comments", comments.content());
        req.getRequestDispatcher("/WEB-INF/views/book-details.jsp").forward(req, resp);
    }

    private void listComments(HttpServletRequest req, HttpServletResponse resp, long bookId) throws IOException {
        PageRequest pageRequest = parsePageRequest(req, Set.of("created_at"), "page", "size", 0, 10, "created_at");
        Page<Comment> comments = commentService.getComments(bookId, pageRequest);
        writeJson(resp, HttpServletResponse.SC_OK, comments);
    }

    private void createComment(HttpServletRequest req, HttpServletResponse resp, long bookId) throws IOException {
        CreateCommentRequest dto;
        String contentType = Optional.ofNullable(req.getContentType()).orElse("");
        if (contentType.startsWith("application/json")) {
            dto = objectMapper.readValue(req.getInputStream(), CreateCommentRequest.class);
        } else {
            dto = new CreateCommentRequest(req.getParameter("author"), req.getParameter("text"));
        }
        Comment saved = commentService.addComment(bookId, dto.author(), dto.text());
        if (acceptsHtml(req)) {
            resp.sendRedirect(req.getContextPath() + "/books/" + bookId);
        } else {
            writeJson(resp, HttpServletResponse.SC_CREATED, saved);
        }
    }

    private PageRequest parsePageRequest(HttpServletRequest req,
                                         Set<String> allowedSortFields,
                                         String pageParam,
                                         String sizeParam,
                                         int defaultPage,
                                         int defaultSize,
                                         String defaultSortField) {
        Map<String, String> errors = new HashMap<>();
        int page = parseInt(req.getParameter(pageParam), defaultPage, pageParam, errors, true);
        int size = parseInt(req.getParameter(sizeParam), defaultSize, sizeParam, errors, false);
        Sort sort = parseSort(req.getParameter("sort"), allowedSortFields, defaultSortField, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Pagination parameters are invalid", errors);
        }
        return new PageRequest(page, size, sort);
    }

    private int parseInt(String raw, int defaultValue, String field, Map<String, String> errors, boolean allowZero) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value < 0 || (!allowZero && value == 0)) {
                errors.put(field, "must be positive");
            }
            return value;
        } catch (NumberFormatException e) {
            errors.put(field, "must be a number");
            return defaultValue;
        }
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

    private PathParts parsePath(String rawPath) {
        String[] segments = rawPath.split("/");
        if (segments.length < 2 || segments[1].isBlank()) {
            throw new IllegalArgumentException("Book id is required");
        }
        long bookId = Long.parseLong(segments[1]);
        if (segments.length > 2 && !segments[2].isBlank() && !"comments".equals(segments[2])) {
            throw new IllegalArgumentException("Unsupported path");
        }
        boolean comments = segments.length > 2 && "comments".equals(segments[2]);
        return new PathParts(bookId, comments);
    }

    private record CreateCommentRequest(String author, String text) {
    }

    private record PathParts(long bookId, boolean comments) {
    }

    private boolean acceptsHtml(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
