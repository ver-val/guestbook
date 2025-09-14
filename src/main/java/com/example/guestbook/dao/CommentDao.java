package com.example.guestbook.dao;

import com.example.guestbook.db.Db;
import com.example.guestbook.model.Comment;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {

    public long insert(String author, String text) throws SQLException {
        String sql = "INSERT INTO comments(author, text) VALUES (?, ?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, author);
            ps.setString(2, text);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public List<Comment> latest() throws SQLException {
        String sql = "SELECT id, author, text, created_at FROM comments ORDER BY id DESC";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Comment> out = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                String author = rs.getString("author");
                String text = rs.getString("text");

                Timestamp ts = rs.getTimestamp("created_at");
                OffsetDateTime createdAt = ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;

                out.add(new Comment(id, author, text, createdAt));
            }
            return out;
        }
    }
}
