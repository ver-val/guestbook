package com.example.guestbook.persistence.jdbc.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DbInit {
    private static final Logger log = LoggerFactory.getLogger(DbInit.class);
    private final DataSource dataSource;

    public DbInit(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize(boolean populateData) {
        try (InputStream stream = getClass().getResourceAsStream("/schema.sql")) {
            if (stream == null) {
                throw new IllegalStateException("schema.sql not found on classpath");
            }
            String sql = new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            executeStatements(sql);
            log.info("Database schema initialized");

            if (populateData && isBooksTableEmpty()) {
                loadData();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema resources", e);
        }
    }

    private void executeStatements(String script) {
        String[] statements = script.split(";");
        try (Connection connection = dataSource.getConnection()) {
            for (String statementSql : statements) {
                String trimmed = statementSql.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }

    private boolean isBooksTableEmpty() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             var rs = statement.executeQuery("SELECT COUNT(*) FROM books")) {
            return rs.next() && rs.getLong(1) == 0;
        } catch (SQLException e) {
            log.warn("Failed to check books table size", e);
            return false;
        }
    }

    private void loadData() {
        try (InputStream dataStream = getClass().getResourceAsStream("/data.sql")) {
            if (dataStream == null) {
                return;
            }
            String dataSql = new BufferedReader(new InputStreamReader(dataStream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            executeStatements(dataSql);
            log.info("Sample data loaded");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read data.sql", e);
        }
    }
}
