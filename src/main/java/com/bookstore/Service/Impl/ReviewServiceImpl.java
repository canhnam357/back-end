package com.bookstore.Service.Impl;

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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    @Override
    public ResponseEntity<GenericResponse> addReview(String authorizationHeader, String bookId, Req_Create_Review review) {
        try {
            String accessToken = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            System.err.println("ADD REVIEW , USERID " + userId);
            System.err.println("BOOK NAME " + bookRepository.findById(bookId).get().getBookName());
            Review new_review = new Review();
            new_review.setBook(bookRepository.findById(bookId).get());
            new_review.setUser(userRepository.findById(userId).get());
            System.err.println("CONTENT " + review.getContent());
            new_review.setContent(review.getContent());
            new_review.setRating(review.getRating());
            Review temp = reviewRepository.save(new_review);
            Res_Get_Review res = new Res_Get_Review();
            res.setReviewId(temp.getReviewId());
            res.setUserId(temp.getUser().getUserId());
            res.setUserReviewed(temp.getUser().getFullName());
            res.setContent(temp.getContent());
            res.setRating(temp.getRating());
            res.setCreatedAt(temp.getCreatedAt());
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Add review successfully!!!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(res)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Add review failed!!! " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, String bookId, int rating) {
        try {
            List<Res_Get_Review> res = new ArrayList<>();
            Page<Review> reviews;
            if (rating == 0) {
                reviews = reviewRepository.findAllByBookBookIdOrderByCreatedAtDesc(PageRequest.of(page - 1, size), bookId);
                for (Review review : reviews) {
                    res.add(new Res_Get_Review(
                            review.getReviewId(),
                            review.getUser().getUserId(),
                            review.getUser().getFullName(),
                            review.getContent(),
                            review.getRating(),
                            review.getCreatedAt()
                    ));
                }
            }
            else {
                reviews = reviewRepository.findByBookIdAndRating(bookId, rating, PageRequest.of(page - 1, size));
                for (Review review : reviews) {
                    res.add(new Res_Get_Review(
                            review.getReviewId(),
                            review.getUser().getUserId(),
                            review.getUser().getFullName(),
                            review.getContent(),
                            review.getRating(),
                            review.getCreatedAt()
                    ));
                }
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
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String reviewId, String token, Req_Create_Review review) {
        try {
            String accessToken = token.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                Optional<User> user_ = reviewRepository.findUserByReviewId(reviewId);
                Optional<Review> review_ = reviewRepository.findById(reviewId);
                System.err.println("UPDATE REVIEW USERID " + user_.get().getUserId());
                if (user_.isPresent() && user_.get().getUserId() == userId) {
                    review_.get().setRating(review.getRating());
                    review_.get().setContent(review.getContent());
                    Review temp = reviewRepository.save(review_.get());
                    Res_Get_Review res = new Res_Get_Review();
                    res.setReviewId(temp.getReviewId());
                    res.setUserId(temp.getUser().getUserId());
                    res.setUserReviewed(temp.getUser().getFullName());
                    res.setContent(temp.getContent());
                    res.setRating(temp.getRating());
                    res.setCreatedAt(temp.getCreatedAt());
                    return ResponseEntity.ok().body(GenericResponse.builder()
                            .message("Update review success !!!")
                            .statusCode(HttpStatus.OK.value())
                            .result(res)
                            .success(true)
                            .build());
                }
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found review !!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .message("Not found user !!!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .success(false)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update review failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String reviewId, String token) {
        try {
            String accessToken = token.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                Optional<User> user_ = reviewRepository.findUserByReviewId(reviewId);
                if (user_.isPresent() && user_.get().getUserId() == userId) {
                    reviewRepository.deleteById(reviewId);
                    return ResponseEntity.ok().body(GenericResponse.builder()
                            .message("Delete review success !!!")
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build());
                }
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found review !!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .message("Not found user !!!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .success(false)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete review failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
