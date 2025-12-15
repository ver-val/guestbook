package com.example.guestbook.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.time.Year;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appName", "Guestbook");
        model.addAttribute("currentYear", Year.now().getValue());
    }
}
