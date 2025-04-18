package com.bookstore.Controller.General;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAllBook(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "100") int size) {
        System.err.println("Get all Book from general - default 100 / page");
        return bookService.getAllBookNotDeleted(page, size);
    }

    @GetMapping("/new_arrivals")
    public ResponseEntity<GenericResponse> getAllBookNewArrivals(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "100") int size) {
        System.err.println("Get all Book from general - default 100 / page");
        return bookService.getAllBookNotDeleted(page, size);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<GenericResponse> getById(@PathVariable String bookId) {
        return bookService.getByIdNotDeleted(bookId);
    }
}
