package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.Admin_Req_Update_Book;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/books")
public class Admin_BookController {

    @Autowired
    private BookService bookService;

    @PostMapping("")
    public ResponseEntity<GenericResponse> createBook (@RequestBody Admin_Req_Create_Book createBook)  {
        System.out.println("ADMIN create Book");
        return bookService.create(createBook);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        System.out.println("ADMIN get all Book");
        return bookService.getAll(page, size);
    }

    @PostMapping("/upload")
    public ResponseEntity<GenericResponse> uploadImage(@RequestParam("image") MultipartFile file,
                                                       @RequestParam("bookId") String bookId,
                                                       @RequestParam(value = "isThumbnail", defaultValue = "0", required = false) int isThumbnail){
        System.out.println("ADMIN upload image");
        return bookService.upload(file, bookId, isThumbnail);
    }

    @GetMapping("/author_books/{authorId}")
    public ResponseEntity<GenericResponse> getAllBooksOfAuthor (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @PathVariable String authorId) {
        System.out.println("ADMIN get all Books of Author");
        return bookService.adminGetBooksOfAuthor(page, size, authorId);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<GenericResponse> deleteBook (@PathVariable String bookId) {
        return bookService.delete(bookId);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<GenericResponse> updateBook (@PathVariable String bookId, @RequestBody Admin_Req_Update_Book book) {
        return bookService.update(bookId, book);
    }
}
