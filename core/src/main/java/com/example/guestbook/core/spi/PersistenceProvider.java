package com.example.guestbook.core.spi;

import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.core.port.CommentRepositoryPort;

public interface PersistenceProvider extends AutoCloseable {
    void initialize(PersistenceConfig config);

    CatalogRepositoryPort catalogRepository();

    CommentRepositoryPort commentRepository();

    @Override
    void close();
}
