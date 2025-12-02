package com.example.guestbook.persistence.jdbc;

import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.core.port.CommentRepositoryPort;
import com.example.guestbook.core.spi.PersistenceConfig;
import com.example.guestbook.core.spi.PersistenceProvider;
import com.example.guestbook.persistence.jdbc.db.DbInit;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class H2PersistenceProvider implements PersistenceProvider {
    private static final Logger log = LoggerFactory.getLogger(H2PersistenceProvider.class);

    private DataSource dataSource;
    private CatalogRepositoryPort catalogRepository;
    private CommentRepositoryPort commentRepository;

    @Override
    public void initialize(PersistenceConfig config) {
        ensureDataDirectory(config.jdbcUrl());
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(config.jdbcUrl());
        ds.setUser(config.username());
        ds.setPassword(config.password());

        this.dataSource = ds;
        new DbInit(dataSource).initialize(config.initializeData());

        this.catalogRepository = new JdbcCatalogRepository(dataSource);
        this.commentRepository = new JdbcCommentRepository(dataSource);
        log.info("Persistence provider initialized (url={})", config.jdbcUrl());
    }

    @Override
    public CatalogRepositoryPort catalogRepository() {
        return catalogRepository;
    }

    @Override
    public CommentRepositoryPort commentRepository() {
        return commentRepository;
    }

    private void ensureDataDirectory(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:h2:file:")) {
            return;
        }
        String withoutPrefix = jdbcUrl.substring("jdbc:h2:file:".length());
        String pathPart = withoutPrefix.split(";")[0];
        Path dbPath = Path.of(pathPart).toAbsolutePath();
        Path dir = dbPath.getParent();
        if (dir != null) {
            try {
                Files.createDirectories(dir);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create data directory: " + dir, e);
            }
        }
    }

    @Override
    public void close() {
        if (dataSource == null) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("SHUTDOWN");
        } catch (SQLException e) {
            log.warn("Failed to gracefully shutdown H2", e);
        }
    }
}
