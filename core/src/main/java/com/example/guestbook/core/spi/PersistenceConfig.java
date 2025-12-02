package com.example.guestbook.core.spi;

public record PersistenceConfig(
        String jdbcUrl,
        String username,
        String password,
        boolean initializeData
) {
    public static PersistenceConfig defaultConfig() {
        return new PersistenceConfig("jdbc:h2:file:./data/library;AUTO_SERVER=TRUE", "sa", "", true);
    }
}
