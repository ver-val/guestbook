package com.example.guestbook.web.util;

import com.example.guestbook.core.service.CatalogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppInfo {

    private final String appName;
    private final CatalogService catalogService;

    public AppInfo(@Value("${spring.application.name:guestbook}") String appName,
                   CatalogService catalogService) {
        this.appName = appName;
        this.catalogService = catalogService;
    }

    public String describe() {
        return "Application '" + appName + "' is ready with catalog service bean: " + (catalogService != null);
    }
}
