package com.example.guestbook.web.error;

import com.example.guestbook.core.exception.BookNotFoundException;
import com.example.guestbook.core.exception.CommentTooOldException;
import com.example.guestbook.core.exception.ForbiddenCommentContentException;
import com.example.guestbook.core.exception.InvalidCommentDeleteException;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    record ErrorResponse(String type, String error, Map<String, String> details) {}

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String type, String message) {
        return error(status, type, message, null);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String type, String message, Map<String, String> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(type, message, details));
    }

    @ExceptionHandler(InvalidCommentDeleteException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDelete(InvalidCommentDeleteException ex) {
        log.warn("Invalid comment delete request: {}", ex.getErrors());
        return error(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(CommentTooOldException.class)
    public ResponseEntity<ErrorResponse> handleTooOld(CommentTooOldException ex) {
        log.warn("Comment too old to delete: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler(ForbiddenCommentContentException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenContent(ForbiddenCommentContentException ex) {
        log.warn("Forbidden content in comment: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
        log.info("Book not found: {}", ex.getMessage());
        return error(HttpStatus.NOT_FOUND, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage());
        return error(HttpStatus.NOT_FOUND, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "Missing required parameter: " + ex.getParameterName();
        log.warn(msg);
        return error(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), msg);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return error(HttpStatus.CONFLICT, ex.getClass().getSimpleName(), ex.getMessage());
    }
}
