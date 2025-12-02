package com.example.guestbook.core.domain;

import java.util.Locale;

public record Sort(String field, Direction direction) {

    public Sort {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("sort field required");
        }
        direction = direction == null ? Direction.ASC : direction;
    }

    public static Sort by(String field, Direction direction) {
        return new Sort(field, direction);
    }

    public static Direction parseDirection(String raw) {
        if (raw == null || raw.isBlank()) {
            return Direction.ASC;
        }
        return Direction.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    public enum Direction {
        ASC, DESC
    }
}
