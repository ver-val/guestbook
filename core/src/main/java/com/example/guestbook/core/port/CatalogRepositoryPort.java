package com.example.guestbook.core.port;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;

import java.util.Optional;

public interface CatalogRepositoryPort {
    Page<Book> findBooks(String query, PageRequest pageRequest, Sort sort);

    Optional<Book> findById(long id);

    boolean existsById(long id);

    Book save(Book book);

    boolean deleteById(long id);
}
