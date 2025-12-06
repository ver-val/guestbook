package com.example.guestbook.web.infrastructure;

import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import com.example.guestbook.core.spi.PersistenceConfig;
import com.example.guestbook.persistence.jdbc.JdbcCatalogRepository;
import com.example.guestbook.persistence.jdbc.JdbcCommentRepository;
import com.example.guestbook.persistence.jdbc.db.DbInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Manual wiring for Javalin (no DI container).
 */
public final class ApplicationInitializer {

    private static final PersistenceConfig persistenceConfig = buildConfig();
    private static final DataSource dataSource = buildDataSource(persistenceConfig);
    private static final JdbcCatalogRepository catalogRepository = new JdbcCatalogRepository(dataSource);
    private static final JdbcCommentRepository commentRepository = new JdbcCommentRepository(dataSource);
    private static final CatalogService catalogService = new CatalogService(catalogRepository);
    private static final CommentService commentService = new CommentService(commentRepository, catalogRepository);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();

    private ApplicationInitializer() {
    }

    public static CatalogService catalogService() {
        return catalogService;
    }

    public static CommentService commentService() {
        return commentService;
    }

    public static ObjectMapper objectMapper() {
        return objectMapper;
    }

    private static PersistenceConfig buildConfig() {
        String url = Optional.ofNullable(System.getenv("DB_URL"))
                .orElse("jdbc:h2:file:../data/library;AUTO_SERVER=TRUE");
        String user = Optional.ofNullable(System.getenv("DB_USER")).orElse("sa");
        String password = Optional.ofNullable(System.getenv("DB_PASSWORD")).orElse("");
        boolean initData = !"false".equalsIgnoreCase(System.getenv("DB_INIT_DATA"));
        return new PersistenceConfig(url, user, password, initData);
    }

    private static DataSource buildDataSource(PersistenceConfig config) {
        ensureDataDirectory(config.jdbcUrl());
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(config.jdbcUrl());
        ds.setUser(config.username());
        ds.setPassword(config.password());

        new DbInit(ds).initialize(config.initializeData());
        return ds;
    }

    private static void ensureDataDirectory(String jdbcUrl) {
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
}
