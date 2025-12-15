package com.example.guestbook.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventsListener implements
        ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventsListener.class);

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        log.info("Login success: user={}, authorities={}", auth.getName(), auth.getAuthorities());
    }

    @Component
    public static class FailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {
        private static final Logger log = LoggerFactory.getLogger(FailureListener.class);

        @Override
        public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
            log.warn("Login failed: user={}", event.getAuthentication().getName(), event.getException());
        }
    }
}
