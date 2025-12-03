package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
    private final CatalogRepositoryPort catalogRepository;

    public CatalogService(CatalogRepositoryPort catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public Page<Book> searchBooks(String query, PageRequest pageRequest, Sort sort) {
        Sort effectiveSort = sort == null ? Sort.by("id", Sort.Direction.ASC) : sort;
        return catalogRepository.findBooks(query, pageRequest, effectiveSort);
    }

    public Book getBook(long id) {
        return catalogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    public void ensureBookExists(long id) {
        if (!catalogRepository.existsById(id)) {
            throw new NotFoundException("Book not found: " + id);
        }
    }
}
