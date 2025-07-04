package com.bookstore.Controller.General;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAllBook(@RequestParam(required = false) BigDecimal minPrice,
                                                      @RequestParam(required = false) BigDecimal maxPrice,
                                                      @RequestParam(defaultValue = "") String authorId,
                                                      @RequestParam(defaultValue = "") String categoryId,
                                                      @RequestParam(defaultValue = "") String publisherId,
                                                      @RequestParam(defaultValue = "") String distributorId,
                                                      @RequestParam(defaultValue = "") String bookName,
                                                      @RequestParam(required = false) String sort, // "asc" | "desc"
                                                      @RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size) {
        return bookService.getAllBookNotDeleted(index, size, minPrice, maxPrice, authorId, publisherId, distributorId, bookName, sort, categoryId);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<GenericResponse> getById(@PathVariable String bookId) {
        return bookService.getByIdNotDeleted(bookId);
    }

    @GetMapping("/price-range")
    public ResponseEntity<GenericResponse> getPriceRange() {
        return bookService.getPriceRange();
    }

    @GetMapping("/search")
    public ResponseEntity<GenericResponse> search(@RequestParam(defaultValue = "") String keyword) {
        return bookService.search(keyword);
    }

    @GetMapping("/new_arrivals")
    public ResponseEntity<GenericResponse> getNewArrivals() {
        return bookService.getNewArrivalsBook();
    }

    @GetMapping("/discount_books")
    public ResponseEntity<GenericResponse> getDiscountBooks() {
        return bookService.getDiscountBook();
    }

    @GetMapping("/high_rating")
    public ResponseEntity<GenericResponse> getHighRatingBooks() {
        return bookService.getHighRatingBook();
    }

    @GetMapping("/most_popular")
    public ResponseEntity<GenericResponse> getMostPopularBooks() {
        return bookService.getMostPopularBooks();
    }

    @GetMapping("/category_most_sold")
    public ResponseEntity<GenericResponse> getBooksInCategoriesMostSold() {
        return bookService.getBooksInCategoriesMostSold();
    }
}
