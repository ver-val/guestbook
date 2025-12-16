package com.example.guestbook.web;

import com.example.guestbook.core.exception.CommentTooOldException;
import com.example.guestbook.web.AppInit;
import com.example.guestbook.core.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppInit.class)
@AutoConfigureMockMvc
class CommentDeleteExceptionIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void shouldReturn400WhenCommentTooOld() throws Exception {
        String message = "Коментар створено більше ніж 24 години тому і не може бути видалений";
        doThrow(new CommentTooOldException(message)).when(commentService).deleteComment(2L);

        mockMvc.perform(delete("/api/comments/{id}", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(message));
    }
}
