package com.example.guestbook.core.exception;

public class BookNotFoundException extends DomainException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
