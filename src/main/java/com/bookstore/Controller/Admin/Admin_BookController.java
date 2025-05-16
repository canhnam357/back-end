package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.Admin_Req_Update_Book;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class Admin_BookController {

    private final BookService bookService;

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createBook (@ModelAttribute Admin_Req_Create_Book createBook)  {
        return bookService.create(createBook);
    }

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return bookService.getAll(index, size, keyword);
    }

    @GetMapping("/author_books/{authorId}") // OK
    public ResponseEntity<GenericResponse> getAllBooksOfAuthor (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @PathVariable String authorId) {
        return bookService.adminGetBooksOfAuthor(index, size, authorId);
    }

    @PutMapping("/{bookId}") // OK
    public ResponseEntity<GenericResponse> updateBook (@PathVariable String bookId, @ModelAttribute Admin_Req_Update_Book book) {
        return bookService.update(bookId, book);
    }
}
