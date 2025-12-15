package com.example.guestbook.web.mail;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateProcessor templateProcessor;
    private final String fromAddress;
    private final String confirmBaseUrl;

    public MailService(JavaMailSender mailSender,
                       EmailTemplateProcessor templateProcessor,
                       @Value("${app.mail.from:}") String fromAddress,
                       @Value("${app.confirm.base-url:http://localhost:8080}") String confirmBaseUrl) {
        this.mailSender = mailSender;
        this.templateProcessor = templateProcessor;
        this.fromAddress = fromAddress;
        this.confirmBaseUrl = confirmBaseUrl;
    }

    public void sendNewBookEmail(Book book) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<String, Object> model = new HashMap<>();
        model.put("title", book.title());
        model.put("author", book.author());
        model.put("year", book.pubYear());
        model.put("added", LocalDateTime.now().format(fmt));

        String html = templateProcessor.process("new_book.ftl", model);

        // kept for backward compatibility: send to fromAddress only
        sendNewBookEmailToRecipient(book, fromAddress);
    }

    @Async
    public void sendNewBookEmailToRecipients(Book book, Iterable<String> recipients) {
        for (String recipient : recipients) {
            try {
                sendNewBookEmailToRecipient(book, recipient);
            } catch (Exception e) {
                log.error("Failed to send new book email to {}", recipient, e);
            }
        }
    }

    private void sendNewBookEmailToRecipient(Book book, String recipient) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<String, Object> model = new HashMap<>();
        model.put("title", book.title());
        model.put("author", book.author());
        model.put("year", book.pubYear());
        model.put("added", LocalDateTime.now().format(fmt));

        String html = templateProcessor.process("new_book.ftl", model);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);

            helper.setTo(recipient);
            helper.setSubject("Нова книга в каталозі");
            helper.setText(html, true);
            helper.setFrom(fromAddress);

            mailSender.send(message);
            log.info("New book email sent to {}: title='{}'", recipient, book.title());
        } catch (Exception e) {
            throw new RuntimeException("Email sending failed to " + recipient, e);
        }
    }

    public void sendConfirmationEmail(User user, String token) {
        Map<String, Object> model = new HashMap<>();
        model.put("username", user.username());
        model.put("confirmLink", confirmBaseUrl + "/confirm?code=" + token);
        model.put("support", fromAddress);

        String html = templateProcessor.process("confirm_account.ftl", model);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);
            helper.setTo(user.email());
            helper.setSubject("Підтвердження акаунту");
            helper.setText(html, true);
            helper.setFrom(fromAddress);
            mailSender.send(message);
            log.info("Confirmation email sent to user='{}' email='{}'", user.username(), user.email());
        } catch (Exception e) {
            log.error("Confirmation email sending failed for user='{}'", user.username(), e);
            throw new RuntimeException("Confirmation email sending failed", e);
        }
    }
}
