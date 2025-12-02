package com.example.guestbook.core.exception;

import java.util.Collections;
import java.util.Map;

public class ValidationException extends DomainException {
    private final Map<String, String> errors;

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors == null ? Collections.emptyMap() : Collections.unmodifiableMap(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
