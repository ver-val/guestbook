package com.example.guestbook.web.config;

import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.core.port.CommentRepositoryPort;
import com.example.guestbook.core.port.UserRepositoryPort;
import com.example.guestbook.core.service.CommentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public CommentService commentService(CommentRepositoryPort commentRepository,
                                         CatalogRepositoryPort catalogRepository,
                                         UserRepositoryPort userRepository) {
        return new CommentService(commentRepository, catalogRepository, userRepository);
    }
}
