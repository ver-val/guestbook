package com.example.guestbook.web.demo;

import com.example.guestbook.core.domain.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AddBooksDemo {
    private static final Logger log = LoggerFactory.getLogger(AddBooksDemo.class);
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT",
            System.getProperty("port", "8080")));

    public static void main(String[] args) throws Exception {
        var books = List.of(
                new Book(0L, "The Hobbit", "J.R.R. Tolkien", "1937"),
                new Book(0L, "It", "Stephen King", "1986"),
                new Book(0L, "1984", "George Orwell", "1949")
        );

        var mapper = new ObjectMapper();
        String endpoint = "http://localhost:" + PORT + "/api/books";
        log.info("Posting sample books to {}", endpoint);

        for (Book b : books) {
            URL url = new URL(endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setConnectTimeout(2000);
            con.setReadTimeout(5000);

            try (OutputStream os = con.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(b));
            }

            int code = con.getResponseCode();
            if (code >= 200 && code < 300) {
                log.info("→ {} : {}", b.title(), code);
            } else {
                String body = readBody(con);
                log.warn("→ {} : {} | {}", b.title(), code, body);
            }
            con.disconnect();
        }

        try {
            String catalog = new String(
                    new URL(endpoint).openStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            log.info("Catalog:\n{}", catalog);
        } catch (Exception e) {
            log.error("Failed to read catalog from {}", endpoint, e);
        }
    }

    private static String readBody(HttpURLConnection con) {
        try {
            if (con.getErrorStream() != null) {
                return new String(con.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            if (con.getInputStream() != null) {
                return new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }
        return "<no body>";
    }
}
