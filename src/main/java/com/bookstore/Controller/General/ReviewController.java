package com.bookstore.Controller.General;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Review;
import com.bookstore.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    @GetMapping("/{bookId}")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "0") int rating,
                                                   @PathVariable String bookId) {
        return reviewService.getAll(index, size, bookId, rating);
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<GenericResponse> addReview (@RequestHeader("Authorization") String authorizationHeader,
                                                      @PathVariable String bookId,
                                                      @RequestBody Req_Create_Review review)  {
        return reviewService.addReview(authorizationHeader, bookId, review);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<GenericResponse> updateReview (@RequestHeader("Authorization") String authorizationHeader,
                                                      @PathVariable String reviewId,
                                                      @RequestBody Req_Create_Review review)  {
        return reviewService.update(reviewId, authorizationHeader, review);
    }
}
