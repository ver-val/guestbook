package com.example.guestbook.core.domain;

public record Book(
        long id,
        String title,
        String author,
        String description,
        Integer pubYear
) {
}
