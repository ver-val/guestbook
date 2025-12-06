package com.example.guestbook.web.routes;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.service.CatalogService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Simple helper endpoint to seed demo data without running the external AddBooksDemo client.
 */
public class DemoRoutes {
    private static final Logger log = LoggerFactory.getLogger(DemoRoutes.class);

    private final CatalogService catalogService;

    public DemoRoutes(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public void register(Javalin app) {
        app.post("/api/demo/seed", this::seedBooks);
    }

    private void seedBooks(Context ctx) {
        List<Book> samples = List.of(
                new Book(0L, "The Hobbit", "J.R.R. Tolkien", "1937"),
                new Book(0L, "It", "Stephen King", "1986"),
                new Book(0L, "1984", "George Orwell", "1949")
        );
        samples.forEach(b -> {
            catalogService.addBook(b.title(), b.author(), b.description());
            log.info("Seeded book: {}", b.title());
        });
        ctx.status(201).json(samples);
    }
}
