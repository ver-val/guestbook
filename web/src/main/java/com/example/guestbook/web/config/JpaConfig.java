package com.example.guestbook.web.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.guestbook.persistence.jpa.repo")
@EntityScan(basePackages = "com.example.guestbook.persistence.jpa.entity")
public class JpaConfig {
}
