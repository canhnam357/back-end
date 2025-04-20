package com.bookstore.Controller.General;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Review;
import com.bookstore.Service.PublisherService;
import com.bookstore.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/{bookId}")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @PathVariable String bookId) {
        return reviewService.getAll(page, size, bookId);
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<GenericResponse> addReview (@RequestHeader("Authorization") String authorizationHeader,
                                                      @PathVariable String bookId,
                                                      @RequestBody Req_Create_Review review)  {
        return reviewService.addReview(authorizationHeader, bookId, review);
    }

}
