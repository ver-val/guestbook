package com.example.guestbook.web.http;

import com.example.guestbook.core.exception.ConflictException;
import com.example.guestbook.core.exception.DomainException;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.web.error.ErrorResponse;
import com.example.guestbook.web.http.config.ApplicationInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
    protected Logger log = LoggerFactory.getLogger(getClass());
    protected ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Object mapper = config.getServletContext().getAttribute(ApplicationInitializer.OBJECT_MAPPER_ATTR);
        if (mapper instanceof ObjectMapper m) {
            this.objectMapper = m;
        } else {
            this.objectMapper = new ObjectMapper().findAndRegisterModules();
        }
    }

    protected void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(resp.getOutputStream(), body);
    }

    protected void handleException(HttpServletRequest req, HttpServletResponse resp, Exception ex) throws IOException {
        int status = 500;
        String message = ex.getMessage();
        Map<String, String> details = Collections.emptyMap();
        if (ex instanceof ValidationException ve) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            details = ve.getErrors();
        } else if (ex instanceof NotFoundException) {
            status = HttpServletResponse.SC_NOT_FOUND;
        } else if (ex instanceof ConflictException) {
            status = HttpServletResponse.SC_CONFLICT;
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
        } else if (ex instanceof DomainException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
        }

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status,
                statusText(status),
                message,
                req.getRequestURI(),
                details
        );

        if (status >= 500) {
            log.error("Request failed: {} {}", status, message, ex);
        } else {
            log.warn("Request failed: {} {} (path={})", status, message, req.getRequestURI());
        }
        writeJson(resp, status, error);
    }

    private String statusText(int status) {
        return switch (status) {
            case HttpServletResponse.SC_BAD_REQUEST -> "Bad Request";
            case HttpServletResponse.SC_NOT_FOUND -> "Not Found";
            case HttpServletResponse.SC_CONFLICT -> "Conflict";
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> "Error";
        };
    }
}
