package com.example.guestbook.web.controller;

import com.example.guestbook.core.domain.Book;
import com.example.guestbook.core.domain.Comment;
import com.example.guestbook.core.domain.PageRequest;
import com.example.guestbook.core.domain.Sort;
import com.example.guestbook.core.service.CatalogService;
import com.example.guestbook.core.service.CommentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping
public class BookPageController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "author");

    private final CatalogService catalogService;
    private final CommentService commentService;

    public BookPageController(CatalogService catalogService, CommentService commentService) {
        this.catalogService = catalogService;
        this.commentService = commentService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String listBooks(@RequestParam(name = "q", required = false) String query, Model model) {
        PageRequest pageRequest = new PageRequest(0, 50, Sort.by("id", Sort.Direction.ASC));
        var books = catalogService.searchBooks(Optional.ofNullable(query).orElse(""), pageRequest, pageRequest.sort());
        model.addAttribute("books", books.content());
        model.addAttribute("q", query == null ? "" : query);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable("id") long bookId, Model model) {
        Book book = catalogService.getBook(bookId);
        PageRequest commentPageRequest = new PageRequest(0, 100, Sort.by("created_at", Sort.Direction.ASC));
        var comments = commentService.getComments(bookId, commentPageRequest);

        model.addAttribute("book", book);
        model.addAttribute("comments", comments.content());
        return "book-details";
    }

    @PostMapping("/books/{id}/comments")
    public String addComment(@PathVariable("id") long bookId,
                             @RequestParam(value = "author", required = false) String author,
                             @RequestParam(value = "text", required = false) String text,
                             RedirectAttributes redirectAttributes) {
        try {
            commentService.addComment(bookId, author, text);
            redirectAttributes.addFlashAttribute("commentSuccess", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }
        return "redirect:/books/" + bookId;
    }

    @PostMapping(value = "/comments/{id}")
    public String deleteComment(@PathVariable("id") long commentId,
                                @RequestParam(name = "bookId") long bookId) {
        commentService.deleteComment(commentId);
        return "redirect:/books/" + bookId;
    }
}
