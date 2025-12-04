package com.example.guestbook.web.config;

import com.example.guestbook.core.spi.PersistenceConfig;
import com.example.guestbook.persistence.jdbc.db.DbInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Configuration
public class DataSourceConfig {

    @Bean
    public PersistenceConfig persistenceConfig() {
        String url = Optional.ofNullable(System.getenv("DB_URL"))
                .orElse("jdbc:h2:file:../data/library;AUTO_SERVER=TRUE");
        String user = Optional.ofNullable(System.getenv("DB_USER")).orElse("sa");
        String password = Optional.ofNullable(System.getenv("DB_PASSWORD")).orElse("");
        boolean initData = !"false".equalsIgnoreCase(System.getenv("DB_INIT_DATA"));
        return new PersistenceConfig(url, user, password, initData);
    }

    @Bean
    public DataSource dataSource(PersistenceConfig config) {
        ensureDataDirectory(config.jdbcUrl());
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(config.jdbcUrl());
        ds.setUser(config.username());
        ds.setPassword(config.password());

        new DbInit(ds).initialize(config.initializeData());
        return ds;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
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
}
