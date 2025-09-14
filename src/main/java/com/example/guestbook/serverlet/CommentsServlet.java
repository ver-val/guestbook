package com.example.guestbook.servlet;

import com.example.guestbook.dao.CommentDao;
import com.example.guestbook.model.Comment;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


@WebServlet(urlPatterns = "/comments")
public class CommentsServlet extends HttpServlet {

  private final CommentDao dao = new CommentDao();
  private final ObjectMapper mapper = new ObjectMapper();
  private static final Logger log = LoggerFactory.getLogger(
    CommentsServlet.class
  );

  @Override
  public void init() throws ServletException {
      try {
          com.example.guestbook.db.DbInit.init();
      } catch (Exception e) {
          throw new ServletException("DB init failed", e);
      }
  }


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
      resp.setContentType("application/json;charset=UTF-8");
      resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

      try {
          List<Comment> comments = dao.latest();

          List<Map<String,Object>> commentsJson = new ArrayList<>();
          for (Comment c : comments) {
              Map<String,Object> m = new HashMap<>();
              m.put("id", c.getId());
              m.put("author", c.getAuthor());
              m.put("text", c.getText());
              m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toInstant().toString() : null);
              commentsJson.add(m);
          }

          mapper.writeValue(resp.getOutputStream(), commentsJson);

      } catch (Exception e) {
          resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          mapper.writeValue(
              resp.getOutputStream(),
              java.util.Map.of(
                  "error", "DB failure",
                  "details", e.getClass().getSimpleName()
              )
          );
      }
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    req.setCharacterEncoding(StandardCharsets.UTF_8.name());
    String author = req.getParameter("author");
    String text = req.getParameter("text");

   if (author == null || author.isBlank() || author.length() > 64 ||
    text == null || text.isBlank() || text.length() > 1000) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType("application/json;charset=UTF-8");
      mapper.writeValue(resp.getOutputStream(), Map.of(
          "error", "Validation failed",
          "author", (author == null || author.isBlank() || author.length() > 64) ? "Поле автор обов'язкове, не більше 64 символи" : null,
          "text", (text == null || text.isBlank() || text.length() > 1000) ? "Поле текст обов'язкове, не більше 1000 символів" : null
      ));
      return;
   }

    try {
      long id = dao.insert(author.trim(), text.trim());
      log.info(
        "New comment: id={}, author='{}', length={}",
        id,
        author.trim(),
        text.trim().length()
      );
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
