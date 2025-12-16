package com.example.guestbook.core.exception;

public class CommentTooOldException extends DomainException {
    public CommentTooOldException(String message) {
        super(message);
    }
}
