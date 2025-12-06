package com.example.guestbook.web.routes;

import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.service.CommentService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

public class CommentsRoutes {
    private final CommentService commentService;

    public CommentsRoutes(CommentService commentService) {
        this.commentService = commentService;
    }

    public void register(Javalin app) {
        app.delete("/api/comments/{id}", this::deleteComment);
    }

    private void deleteComment(Context ctx) {
        long id = parseLong(ctx.pathParam("id"));
        commentService.deleteComment(id);
        ctx.status(204);
    }

    private long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid id", Map.of("id", "must be a number"));
        }
    }

}
