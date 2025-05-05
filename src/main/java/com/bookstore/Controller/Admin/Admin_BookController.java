package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.Admin_Req_Update_Book;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/books")
public class Admin_BookController {

    @Autowired
    private BookService bookService;

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createBook (@ModelAttribute Admin_Req_Create_Book createBook)  {
        System.out.println("ADMIN create Book");
        return bookService.create(createBook);
    }

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        System.out.println("ADMIN get all Book, keyword = " + keyword);
        return bookService.getAll(index, size, keyword);
    }

    @GetMapping("/author_books/{authorId}") // OK
    public ResponseEntity<GenericResponse> getAllBooksOfAuthor (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @PathVariable String authorId) {
        System.out.println("ADMIN get all Books of Author");
        return bookService.adminGetBooksOfAuthor(index, size, authorId);
    }
    // DELETE use updateBook?
//    @DeleteMapping("/{bookId}")
//    public ResponseEntity<GenericResponse> deleteBook (@PathVariable String bookId) {
//        return bookService.delete(bookId);
//    }

    @PutMapping("/{bookId}") // OK
    public ResponseEntity<GenericResponse> updateBook (@PathVariable String bookId, @ModelAttribute Admin_Req_Update_Book book) {
        return bookService.update(bookId, book);
    }
}
