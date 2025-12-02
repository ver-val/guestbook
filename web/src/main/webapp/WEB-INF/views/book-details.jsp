<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="jakarta.servlet.http.HttpServletResponse" %>
<%@ page import="com.example.guestbook.core.domain.Book" %>
<%@ page import="com.example.guestbook.core.domain.Comment" %>
<%
    Book book = (Book) request.getAttribute("book");
    List<Comment> comments = (List<Comment>) request.getAttribute("comments");
    if (book == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
        return;
    }
%>
<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Деталі книги</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
</head>
<body data-base-url="<%=request.getContextPath()%>">
<div class="wrapper" data-book-id="<%=book.id()%>">
    <h1 class="book-title"><strong><%=book.title()%></strong></h1>
    <p class="book-author"><i><%=book.author()%></i></p>
    <p class="book-description"><%=book.description() == null ? "" : book.description()%></p>

    <h3>Коментарі</h3>
    <ul id="commentList" class="comment-list">
        <%
            if (comments == null || comments.isEmpty()) {
        %>
            <li class="comment-row empty">Ще немає коментарів.</li>
        <%
            } else {
                for (Comment c : comments) {
        %>
            <li class="comment-row" data-comment-id="<%=c.id()%>">
                <div class="comment-text">
                    <div class="comment-meta">
                        <span class="comment-author"><%=c.author()%></span>
                        <span class="comment-time"><%=java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(c.createdAt().atZone(java.time.ZoneId.systemDefault()))%></span>
                    </div>
                    <div class="comment-body"><%=c.text()%></div>
                </div>
                <button type="button" class="delete-btn" data-comment-id="<%=c.id()%>" value="<%=c.id()%>">Видалити</button>
            </li>
        <%
                }
            }
        %>
    </ul>

    <h3>Додати коментар</h3>
    <form id="commentForm" method="post" action="<%=request.getContextPath()%>/books/<%=book.id()%>/comments">
        <input type="hidden" id="bookIdInput" value="<%=book.id()%>">
        <label>Автор
            <input type="text" name="author" maxlength="64" required>
        </label>
        <label>Текст
            <textarea name="text" maxlength="1000" required></textarea>
        </label>
        <button type="submit">Додати</button>
    </form>

    <p><a href="<%=request.getContextPath()%>/books">← Повернутись до списку книг</a></p>
</div>
<script src="<%=request.getContextPath()%>/js/book-details.js"></script>
</body>
</html>
