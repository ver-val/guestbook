package com.example.guestbook.web.mail;

import com.example.guestbook.core.domain.Book;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
    private final String toAddress;

    public MailService(JavaMailSender mailSender,
                       EmailTemplateProcessor templateProcessor,
                       @Value("${app.mail.from:}") String fromAddress,
                       @Value("${app.mail.to:}") String toAddress) {
        this.mailSender = mailSender;
        this.templateProcessor = templateProcessor;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
    }

    public void sendNewBookEmail(Book book) {
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

            String recipient = toAddress == null || toAddress.isBlank() ? fromAddress : toAddress;
            helper.setTo(recipient);
            helper.setSubject("Нова книга в каталозі");
            helper.setText(html, true);
            helper.setFrom(fromAddress);

            mailSender.send(message);
            log.info("New book email sent: title='{}', author='{}', year={}", book.title(), book.author(), book.pubYear());
        } catch (Exception e) {
            log.error("Email sending failed for book title='{}'", book.title(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
