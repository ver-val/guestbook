package com.example.guestbook.core.domain;

public record PageRequest(
        int page,
        int size,
        Sort sort
) {
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        sort = sort == null ? Sort.by("id", Sort.Direction.ASC) : sort;
    }

    public int offset() {
        return page * size;
    }
}
