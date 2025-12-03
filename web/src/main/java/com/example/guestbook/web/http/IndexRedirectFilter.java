package com.example.guestbook.web.http;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Redirects only the context root to /books, while letting static resources and other paths through.
 */
public class IndexRedirectFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            String contextPath = req.getContextPath();
            String uri = req.getRequestURI();
            boolean isRoot = uri.equals(contextPath) || uri.equals(contextPath + "/");
            if (isRoot) {
                resp.sendRedirect(contextPath + "/books");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
