package com.example.guestbook.web.controller;

import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.service.CommentService;
import com.example.guestbook.core.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController {

    private final CommentService commentService;
    private final UserService userService;

    public UserController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/users/{id}/comments")
    public String userComments(@PathVariable Long id, Model model) {
        try {
            var user = userService.getUser(id);
            var comments = commentService.getCommentsByUser(id, new PageRequest(0, 50, Sort.by("created_at", Sort.Direction.DESC)));

            model.addAttribute("comments", comments.content());
            model.addAttribute("username", user.username());
            model.addAttribute("userId", user.id());
            return "user-comments";
        } catch (NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }
}
