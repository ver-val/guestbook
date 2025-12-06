package com.example.guestbook.web;

import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ConflictException;
import com.example.guestbook.web.infrastructure.ApplicationInitializer;
import com.example.guestbook.web.routes.BooksRoutes;
import com.example.guestbook.web.routes.CommentsRoutes;
import com.example.guestbook.web.routes.DemoRoutes;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JavalinBookApp {
    private static final Logger log = LoggerFactory.getLogger(JavalinBookApp.class);

    public static void main(String[] args) {
        var jsonMapper = new JavalinJackson(ApplicationInitializer.objectMapper(), false);

        Javalin app = Javalin.create(cfg -> {
            cfg.jsonMapper(jsonMapper);
            cfg.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/public";
                staticFiles.precompress = false;
            });
        });

        app.before(ctx -> {
            ctx.attribute("startNanos", System.nanoTime());
            log.info(">>> {} {}", ctx.method(), ctx.path());
        });

        new BooksRoutes(ApplicationInitializer.catalogService(), ApplicationInitializer.commentService()).register(app);
        new CommentsRoutes(ApplicationInitializer.commentService()).register(app);
        new DemoRoutes(ApplicationInitializer.catalogService()).register(app);

        app.exception(ValidationException.class, (e, ctx) ->
                ctx.status(400).json(error(ctx.path(), 400, e.getMessage(), e.getErrors())));
        app.exception(NotFoundException.class, (e, ctx) ->
                ctx.status(404).json(error(ctx.path(), 404, e.getMessage(), Map.of())));
        app.exception(ConflictException.class, (e, ctx) ->
                ctx.status(409).json(error(ctx.path(), 409, e.getMessage(), Map.of())));
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unexpected error on {} {}", ctx.method(), ctx.path(), e);
            ctx.status(500).json(error(ctx.path(), 500, "Internal server error", Map.of()));
        });

        app.after(ctx -> {
            Long start = ctx.attribute("startNanos");
            long tookMs = start == null ? -1 : (System.nanoTime() - start) / 1_000_000;
            log.info("<<< {} {} -> {} ({} ms)", ctx.method(), ctx.path(), ctx.status(), tookMs);
        });

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT",
                System.getProperty("port", "8080")));
        app.start(port);
        log.info("Server started at http://localhost:{}", port);
    }

    private static ErrorResponse error(String path, int status, String message, Map<String, String> details) {
        return new ErrorResponse(status, message, path, details);
    }

    private record ErrorResponse(int status, String message, String path, Map<String, String> details) {
    }
}
