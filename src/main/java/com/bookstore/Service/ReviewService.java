package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Review;
import org.springframework.http.ResponseEntity;

public interface ReviewService {

    ResponseEntity<GenericResponse> addReview(String authorizationHeader, String bookId, Req_Create_Review review);

    ResponseEntity<GenericResponse> getAll(int page, int size, String bookId, int rating);

    ResponseEntity<GenericResponse> update(String reviewId, String token, Req_Create_Review review);
}
