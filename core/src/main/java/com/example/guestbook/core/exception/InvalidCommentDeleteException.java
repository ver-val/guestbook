package com.example.guestbook.core.exception;

import java.util.Map;

public class InvalidCommentDeleteException extends DomainException {
    private final Map<String, String> errors;

    public InvalidCommentDeleteException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
