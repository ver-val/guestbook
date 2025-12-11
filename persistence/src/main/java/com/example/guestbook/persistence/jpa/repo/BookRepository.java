package com.example.guestbook.persistence.jpa.repo;

import com.example.guestbook.persistence.jpa.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    @Query("""
            select b from BookEntity b
            where lower(b.title) like lower(concat('%', :q, '%'))
               or lower(b.author) like lower(concat('%', :q, '%'))
            """)
    Page<BookEntity> search(@Param("q") String query, Pageable pageable);
}
