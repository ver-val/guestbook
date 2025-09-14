package com.example.guestbook.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = "")
public class IndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html;charset=UTF-8");

        resp.getWriter().write("""
                <!DOCTYPE html>
                <html lang="ua">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Книга відгуків</title>
                    <link rel="stylesheet" href="css/styles.css">
                </head>
                <body>
                    <div class="wrapper">
                        <h1>Книга відгуків</h1>
                        <form id="f">
                            <label>Автор
                                <input name="author" maxlength="64" required />
                                <span class="error" id="authorError"></span>
                            </label>
                            <label>Текст
                                <textarea name="text" maxlength="1000" required rows="4"></textarea>
                                <span class="error" id="textError"></span>
                            </label>
                            <button>Додати</button>
                        </form>
                        <ul id="comments"></ul>
                    </div>
                    <script src="js/script.js"></script>
                </body>
                </html>
                """);
    }
}
