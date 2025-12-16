package com.example.guestbook.core.service;

import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.exception.CommentTooOldException;
import com.example.guestbook.core.exception.ForbiddenCommentContentException;
import com.example.guestbook.core.exception.InvalidCommentDeleteException;
import com.example.guestbook.core.port.CatalogRepositoryPort;
import com.example.guestbook.core.port.CommentRepositoryPort;
import com.example.guestbook.core.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepositoryPort commentRepository;
    @Mock
    CatalogRepositoryPort catalogRepository;
    @Mock
    UserRepositoryPort userRepository;

    CommentService service;

    @BeforeEach
    void setUp() {
        service = new CommentService(commentRepository, catalogRepository, userRepository);
    }

    @Test
    void delete_shouldThrowInvalidCommentDelete_whenIdsInvalid() {
        assertThrows(InvalidCommentDeleteException.class, () -> service.delete(-1, 0));

        verifyNoInteractions(commentRepository);
        verifyNoInteractions(catalogRepository);
    }

    @Test
    void delete_shouldThrowCommentTooOld_whenOlderThan24h() {
        long bookId = 1L;
        long commentId = 2L;
        when(catalogRepository.existsById(bookId)).thenReturn(true);
        Instant oldCreated = Instant.now().minusSeconds(25 * 3600);
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(new Comment(commentId, bookId, "a", "t", oldCreated, null)));

        assertThrows(CommentTooOldException.class, () -> service.delete(bookId, commentId));

        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldInvokeRepositoryOnValidData() {
        long bookId = 1L;
        long commentId = 2L;
        when(catalogRepository.existsById(bookId)).thenReturn(true);
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(new Comment(commentId, bookId, "a", "t", Instant.now(), null)));
        when(commentRepository.deleteById(commentId)).thenReturn(true);

        service.delete(bookId, commentId);

        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void addComment_shouldThrowForbiddenContent_whenBannedWord() {
        assertThrows(ForbiddenCommentContentException.class, () -> service.addComment(1L, "a", "spam text"));
        verify(commentRepository, never()).save(any());
    }
}
