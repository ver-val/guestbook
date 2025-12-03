package com.example.guestbook.persistence.jdbc;

import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.port.CommentRepositoryPort;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCommentRepository implements CommentRepositoryPort {
    private final DataSource dataSource;

    public JdbcCommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Page<Comment> findByBookId(long bookId, PageRequest pageRequest) {
        String baseSql = "FROM comments WHERE book_id = ?";
        String dataSql = "SELECT id, book_id, author, text, created_at " + baseSql +
                " ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getConnection()) {
            long total = count(connection, baseSql, bookId);
            List<Comment> comments = fetch(connection, dataSql, bookId, pageRequest);
            int totalPages = (int) Math.ceil((double) total / pageRequest.size());
            boolean last = pageRequest.page() >= totalPages - 1;
            return new Page<>(comments, pageRequest.page(), pageRequest.size(), total, totalPages, last);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch comments for book " + bookId, e);
        }
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comments(book_id, author, text, created_at) VALUES (?,?,?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, comment.bookId());
            statement.setString(2, comment.author());
            statement.setString(3, comment.text());
            statement.setTimestamp(4, Timestamp.from(comment.createdAt()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new Comment(id, comment.bookId(), comment.author(), comment.text(), comment.createdAt());
                }
            }
            throw new IllegalStateException("Failed to obtain generated id for comment");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save comment", e);
        }
    }

    @Override
    public Optional<Comment> findById(long id) {
        String sql = "SELECT id, book_id, author, text, created_at FROM comments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find comment: " + id, e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete comment: " + id, e);
        }
    }

    private long count(Connection connection, String baseSql, long bookId) throws SQLException {
        String sql = "SELECT COUNT(*) " + baseSql;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private List<Comment> fetch(Connection connection, String sql, long bookId, PageRequest pageRequest) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            statement.setInt(2, pageRequest.size());
            statement.setInt(3, pageRequest.offset());

            try (ResultSet rs = statement.executeQuery()) {
                List<Comment> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        }
    }

    private Comment map(ResultSet rs) throws SQLException {
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        return new Comment(
                rs.getLong("id"),
                rs.getLong("book_id"),
                rs.getString("author"),
                rs.getString("text"),
                createdAt
        );
    }
}
