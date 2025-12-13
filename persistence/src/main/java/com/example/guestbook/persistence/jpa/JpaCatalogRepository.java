package com.example.guestbook.persistence.jpa;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.persistence.jpa.entity.BookEntity;
import com.example.guestbook.persistence.jpa.repo.BookRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public class JpaCatalogRepository implements CatalogRepositoryPort {

    private final BookRepository bookRepository;

    public JpaCatalogRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Page<Book> findBooks(String query, PageRequest pageRequest, Sort sort) {
        Pageable pageable = buildPageable(pageRequest, sort);
        org.springframework.data.domain.Page<BookEntity> page;
        if (query != null && !query.isBlank()) {
            String trimmed = query.trim();
            page = bookRepository.search(trimmed, pageable);
        } else {
            page = bookRepository.findAll(pageable);
        }
        return new Page<>(
                page.map(this::toDomain).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public Optional<Book> findById(long id) {
        return bookRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsById(long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public Book save(Book book) {
        BookEntity entity = toEntity(book);
        BookEntity saved = bookRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean deleteById(long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    private Pageable buildPageable(PageRequest pageRequest, Sort sort) {
        org.springframework.data.domain.Sort springSort;
        if (sort == null || sort.field() == null || sort.field().isBlank()) {
            springSort = org.springframework.data.domain.Sort.by(Direction.ASC, "id");
        } else {
            String field = mapSortField(sort.field());
            springSort = org.springframework.data.domain.Sort.by(
                    sort.direction() == Sort.Direction.DESC ? Direction.DESC : Direction.ASC,
                    field
            );
        }
        return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(), springSort);
    }

    private String mapSortField(String field) {
        return switch (field) {
            case "title" -> "title";
            case "author" -> "author";
            case "pub_year", "pubYear" -> "pubYear";
            default -> "id";
        };
    }

    private Book toDomain(BookEntity entity) {
        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getDescription(),
                entity.getPubYear()
        );
    }

    private BookEntity toEntity(Book book) {
        BookEntity entity = new BookEntity();
        if (book.id() > 0) {
            entity.setId(book.id());
        }
        entity.setTitle(book.title());
        entity.setAuthor(book.author());
        entity.setDescription(book.description());
        entity.setPubYear(book.pubYear());
        return entity;
    }
}
