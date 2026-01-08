package com.example.guestbook.web.controller;

import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.service.AccountConfirmationService;
import com.example.guestbook.core.service.UserService;
import com.example.guestbook.web.mail.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AccountConfirmationService confirmationService;
    private final MailService mailService;
    private final boolean mailEnabled;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          AccountConfirmationService confirmationService,
                          MailService mailService,
                          @org.springframework.beans.factory.annotation.Value("${app.mail.enabled:true}") boolean mailEnabled) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.confirmationService = confirmationService;
        this.mailService = mailService;
        this.mailEnabled = mailEnabled;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "disabled", required = false) String disabled,
                        Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        if (disabled != null) {
            model.addAttribute("loginDisabled", true);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            var created = userService.register(username, email, passwordEncoder.encode(password), "USER");
            var token = confirmationService.createToken(created);
            if (mailEnabled) {
                mailService.sendConfirmationEmail(created, token.token());
                redirectAttributes.addFlashAttribute("confirmEmailSent", true);
            } else {
                confirmationService.confirm(token.token());
                redirectAttributes.addFlashAttribute("confirmed", true);
            }
            redirectAttributes.addFlashAttribute("registered", true);
            return "redirect:/login";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getErrors());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        try {
            confirmationService.confirm(code);
            redirectAttributes.addFlashAttribute("confirmed", true);
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("confirmError", e.getMessage());
            return "redirect:/login";
        }
    }
}
