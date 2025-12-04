package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    public Book addBook(String title, String author, String description) {
        Map<String, String> errors = new HashMap<>();
        if (title == null || title.isBlank() || title.length() > 255) {
            errors.put("title", "required, up to 255 characters");
        }
        if (author == null || author.isBlank() || author.length() > 255) {
            errors.put("author", "required, up to 255 characters");
        }
        if (description != null && description.length() > 2000) {
            errors.put("description", "max 2000 characters");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Book validation failed", errors);
        }
        Book toSave = new Book(0, title.trim(), author.trim(), description == null ? null : description.trim());
        return catalogRepository.save(toSave);
    }
}
