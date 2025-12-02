package com.example.guestbook.web.http;

import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.service.CommentService;
import com.example.guestbook.web.http.config.ApplicationInitializer;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = "/comments/*")
public class CommentServlet extends BaseServlet {
    private transient CommentService commentService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        var context = config.getServletContext();
        this.commentService = (CommentService) context.getAttribute(ApplicationInitializer.COMMENT_SERVICE_ATTR);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            long id = resolveId(req);
            commentService.deleteComment(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception ex) {
            handleException(req, resp, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Allow HTML form with _method=delete as a convenience for JSP
        String methodOverride = req.getParameter("_method");
        if ("delete".equalsIgnoreCase(methodOverride)) {
            doDelete(req, resp);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private long resolveId(HttpServletRequest req) {
        String pathInfo = Optional.ofNullable(req.getPathInfo()).orElse("");
        String fromPath = pathInfo.replaceAll("^/+", "").replaceAll("/+$", "");
        if (!fromPath.isBlank()) {
            return Long.parseLong(fromPath);
        }
        String paramId = Optional.ofNullable(req.getParameter("commentId"))
                .orElse(req.getParameter("id"));
        if (paramId != null && !paramId.isBlank()) {
            return Long.parseLong(paramId);
        }
        // if id is missing entirely, return 404 to avoid generic bad request
        throw new NotFoundException("Comment not found");
    }
}
