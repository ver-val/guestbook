<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.guestbook.core.domain.Book" %>
<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Каталог книг</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
</head>
<body>
<div class="wrapper">
    <h1>Каталог книг</h1>
    <ul>
        <%
            List<Book> books = (List<Book>) request.getAttribute("books");
            if (books != null && !books.isEmpty()) {
                for (Book book : books) {
        %>
                    <li>
                        <strong><%=book.title()%></strong> — <%=book.author()%>
                        <br/>
                        <a href="<%=request.getContextPath()%>/books/<%=book.id()%>">Деталі та коментарі</a>
                    </li>
        <%
                }
            } else {
        %>
                <li>Немає книг</li>
        <%
            }
        %>
    </ul>
</div>
</body>
</html>
