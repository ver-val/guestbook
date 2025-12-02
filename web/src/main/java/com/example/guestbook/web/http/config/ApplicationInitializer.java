package com.example.guestbook.web.http.config;

import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import com.example.guestbook.core.spi.PersistenceConfig;
import com.example.guestbook.core.spi.PersistenceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.ServiceLoader;

@WebListener
public class ApplicationInitializer implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(ApplicationInitializer.class);
    public static final String CATALOG_SERVICE_ATTR = CatalogService.class.getName();
    public static final String COMMENT_SERVICE_ATTR = CommentService.class.getName();
    public static final String OBJECT_MAPPER_ATTR = ObjectMapper.class.getName();
    public static final String PERSISTENCE_PROVIDER_ATTR = PersistenceProvider.class.getName();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        PersistenceProvider provider = ServiceLoader.load(PersistenceProvider.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No PersistenceProvider implementation found on classpath"));

        PersistenceConfig config = loadConfig();
        provider.initialize(config);

        CatalogService catalogService = new CatalogService(provider.catalogRepository());
        CommentService commentService = new CommentService(provider.commentRepository(), provider.catalogRepository());
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        context.setAttribute(CATALOG_SERVICE_ATTR, catalogService);
        context.setAttribute(COMMENT_SERVICE_ATTR, commentService);
        context.setAttribute(OBJECT_MAPPER_ATTR, objectMapper);
        context.setAttribute(PERSISTENCE_PROVIDER_ATTR, provider);
        log.info("Application initialized with DB url {}", config.jdbcUrl());
    }

    private PersistenceConfig loadConfig() {
        String url = Optional.ofNullable(System.getenv("DB_URL"))
                .orElse("jdbc:h2:file:./data/library;AUTO_SERVER=TRUE");
        String user = Optional.ofNullable(System.getenv("DB_USER")).orElse("sa");
        String password = Optional.ofNullable(System.getenv("DB_PASSWORD")).orElse("");
        boolean initData = !"false".equalsIgnoreCase(System.getenv("DB_INIT_DATA"));
        return new PersistenceConfig(url, user, password, initData);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Object providerAttr = context.getAttribute(PERSISTENCE_PROVIDER_ATTR);
        if (providerAttr instanceof PersistenceProvider provider) {
            provider.close();
        }
    }
}
