package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.exception.NotFoundException;
import com.example.guestbook.core.exception.ValidationException;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

@Service
@Transactional
public class CatalogService {
    private final CatalogRepositoryPort catalogRepository;

    public CatalogService(CatalogRepositoryPort catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String query, PageRequest pageRequest, Sort sort) {
        Sort effectiveSort = sort == null ? Sort.by("id", Sort.Direction.ASC) : sort;
        return catalogRepository.findBooks(query, pageRequest, effectiveSort);
    }

    @Transactional(readOnly = true)
    public Book getBook(long id) {
        return catalogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    public void ensureBookExists(long id) {
        if (!catalogRepository.existsById(id)) {
            throw new NotFoundException("Book not found: " + id);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBook(long id) {
        ensureBookExists(id);
        if (!catalogRepository.deleteById(id)) {
            throw new ValidationException("Failed to delete book: " + id, Map.of());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Book addBook(String title, String author, String description, Integer pubYear) {
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
        if (pubYear != null) {
            int currentYear = LocalDate.now().getYear();
            if (pubYear < 1 || pubYear > currentYear) {
                errors.put("pubYear", "must be between 1 and " + currentYear);
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Book validation failed", errors);
        }
        Book toSave = new Book(
                0,
                title.trim(),
                author.trim(),
                description == null ? null : description.trim(),
                pubYear
        );
        return catalogRepository.save(toSave);
    }
}
