package com.example.guestbook.core.domain;

import java.util.Collections;
import java.util.List;

public record Page<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public Page {
        content = content == null ? Collections.emptyList() : List.copyOf(content);
    }
}
