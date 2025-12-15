package com.example.guestbook.web.service;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.User;
import com.example.guestbook.core.service.UserService;
import com.example.guestbook.web.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final MailService mailService;
    private final UserService userService;

    public NotificationService(MailService mailService, UserService userService) {
        this.mailService = mailService;
        this.userService = userService;
    }

    @Async
    public void notifyNewBook(Book book) {
        List<User> recipients = userService.listActiveNonAdminUsers();
        List<String> emails = recipients.stream()
                .map(User::email)
                .filter(e -> e != null && !e.isBlank())
                .toList();
        if (emails.isEmpty()) {
            log.info("No recipients found for new book notification, skipping");
            return;
        }
        mailService.sendNewBookEmailToRecipients(book, emails);
    }
}
