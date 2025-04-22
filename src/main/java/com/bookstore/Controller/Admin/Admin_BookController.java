package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import com.bookstore.Service.CloudinaryService;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("")
    public ResponseEntity<GenericResponse> createBook (@RequestBody Admin_Req_Create_Book createBook)  {
        return bookService.create(createBook);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return bookService.getAll(page, size);
    }

    @PostMapping("/upload")
    public ResponseEntity<GenericResponse> uploadImage(@RequestParam("image") MultipartFile file,
                                                       @RequestParam("bookId") String bookId,
                                                       @RequestParam(value = "isThumbnail", defaultValue = "0", required = false) int isThumbnail){
        return bookService.upload(file, bookId, isThumbnail);
    }

    @GetMapping("/author_books/{authorId}")
    public ResponseEntity<GenericResponse> getAllBookOfAuthor (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @PathVariable String authorId) {
        System.err.println(authorId);
        return bookService.adminGetBooksOfAuthor(page, size, authorId);
    }
}
