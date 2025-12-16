package com.example.guestbook.core.exception;

public class ForbiddenCommentContentException extends DomainException {
    public ForbiddenCommentContentException(String message) {
        super(message);
    }
}
