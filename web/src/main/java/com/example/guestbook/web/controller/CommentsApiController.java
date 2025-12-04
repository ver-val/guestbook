package com.example.guestbook.web.controller;

import com.example.guestbook.core.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {

    private final CommentService commentService;

    public CommentsApiController(CommentService commentService) {
        this.commentService = commentService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
