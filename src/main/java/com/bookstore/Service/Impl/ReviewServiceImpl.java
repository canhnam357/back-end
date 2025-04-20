package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Res_Get_Users;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Review;
import com.bookstore.DTO.Res_Get_Review;
import com.bookstore.Entity.Review;
import com.bookstore.Entity.User;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.ReviewRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public ResponseEntity<GenericResponse> addReview(String authorizationHeader, String bookId, Req_Create_Review review) {
        try {
            String accessToken = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            System.err.println("ADD REVIEW , USERID " + userId);
            Review new_review = new Review();
            new_review.setBook(bookRepository.findById(bookId).get());
            new_review.setUser(userRepository.findById(userId).get());
            new_review.setContent(review.getContent());
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Add review successfully!!!")
                    .result(reviewRepository.save(new_review))
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Add review failed!!!")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, String bookId) {
        try {
            Page<Review> reviews = reviewRepository.findAllByBookBookIdOrderByCreatedAtDesc(PageRequest.of(page - 1, size), bookId);
            List<Res_Get_Review> res = new ArrayList<>();
            for (Review review : reviews) {
                res.add(new Res_Get_Review(
                        review.getUser().getFullName(),
                        review.getContent(),
                        review.getCreatedAt()
                ));
            }

            Page<Res_Get_Review> dtoPage = new PageImpl<>(res, reviews.getPageable(), reviews.getTotalElements());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Get all review successfully!!!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get all review failed!!!")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
