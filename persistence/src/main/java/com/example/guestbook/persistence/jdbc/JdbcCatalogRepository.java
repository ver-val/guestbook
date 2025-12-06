package com.example.guestbook.persistence.jdbc;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Repository
public class JdbcCatalogRepository implements CatalogRepositoryPort {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "author", "pub_year");
    private final DataSource dataSource;

    public JdbcCatalogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Page<Book> findBooks(String query, PageRequest pageRequest, Sort sort) {
        String normalizedSortField = normalizeSortField(sort == null ? null : sort.field());
        String direction = sort == null ? Sort.Direction.ASC.name() : sort.direction().name();
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            where.append(" WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ?");
            String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            params.add(like);
            params.add(like);
        }

        String baseSql = "FROM books" + where;
        String dataSql = "SELECT id, title, author, description, pub_year " + baseSql +
                " ORDER BY " + normalizedSortField + " " + direction +
                " LIMIT ? OFFSET ?";
        params.add(pageRequest.size());
        params.add(pageRequest.offset());

        try (Connection connection = dataSource.getConnection()) {
            long total = count(connection, baseSql, params.subList(0, params.size() - 2));
            List<Book> books = fetchBooks(connection, dataSql, params);

            int totalPages = (int) Math.ceil((double) total / pageRequest.size());
            boolean last = pageRequest.page() >= totalPages - 1;
            return new Page<>(books, pageRequest.page(), pageRequest.size(), total, totalPages, last);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query books", e);
        }
    }

    @Override
    public Optional<Book> findById(long id) {
        String sql = "SELECT id, title, author, description, pub_year FROM books WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapBook(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch book: " + id, e);
        }
    }

    @Override
    public boolean existsById(long id) {
        String sql = "SELECT 1 FROM books WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check book existence: " + id, e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete book: " + id, e);
        }
    }

    @Override
    public Book save(Book book) {
        String sql = "INSERT INTO books (title, author, description, pub_year) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, book.title());
            statement.setString(2, book.author());
            statement.setString(3, book.description());
            if (book.pubYear() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, book.pubYear());
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new Book(id, book.title(), book.author(), book.description(), book.pubYear());
                }
            }
            throw new IllegalStateException("Failed to obtain generated id for book");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save book", e);
        }
    }

    private List<Book> fetchBooks(Connection connection, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<Book> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapBook(rs));
                }
                return result;
            }
        }
    }

    private long count(Connection connection, String baseSql, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) " + baseSql;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private String normalizeSortField(String sortField) {
        if (sortField == null) {
            return "id";
        }
        String normalized = sortField.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_SORT_FIELDS.contains(normalized) ? normalized : "id";
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("description"),
                getInteger(rs, "pub_year")
        );
    }

    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
