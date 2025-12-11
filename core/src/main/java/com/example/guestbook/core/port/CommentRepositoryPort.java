package com.example.guestbook.core.port;

import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.Page;
import com.example.guestbook.core.domain.PageRequest;

import java.util.Optional;

public interface CommentRepositoryPort {
    Page<Comment> findByBookId(long bookId, PageRequest pageRequest);
    Page<Comment> findByUserId(long userId, PageRequest pageRequest);

    Comment save(Comment comment);

    Optional<Comment> findById(long id);

    boolean deleteById(long id);
}
