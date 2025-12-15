package com.example.guestbook.web.controller;

import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Component
public class CurrentUserModelAttribute {

    private final UserService userService;

    public CurrentUserModelAttribute(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return userService.getCurrentUser().orElse(null);
    }
}
