package com.example.guestbook.web.controller;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
public class CommentsController {

    private final CommentService commentService;
    private final CatalogService catalogService;

    public CommentsController(CommentService commentService, CatalogService catalogService) {
        this.commentService = commentService;
        this.catalogService = catalogService;
    }

    @GetMapping
    public String list(@RequestParam long bookId, Model model) {
        Book book;
        try {
            book = catalogService.getBook(bookId);
        } catch (Exception e) {
            return "redirect:/books";
        }

        PageRequest pageRequest = new PageRequest(0, 20, Sort.by("created_at", Sort.Direction.ASC));
        var comments = commentService.getComments(bookId, pageRequest).content();

        model.addAttribute("book", book);
        model.addAttribute("comments", comments);
        return "book-comments";
    }

    @PostMapping
    public String add(@RequestParam long bookId,
                      @RequestParam String author,
                      @RequestParam String text,
                      RedirectAttributes redirectAttributes) {
        try {
            commentService.addComment(bookId, author, text);
            redirectAttributes.addFlashAttribute("commentSuccess", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }
        return "redirect:/comments?bookId=" + bookId;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam long bookId,
                         @RequestParam long commentId,
                         RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(commentId);
            redirectAttributes.addFlashAttribute("commentDeleted", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }
        return "redirect:/comments?bookId=" + bookId;
    }
}
