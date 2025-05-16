package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Review;
import com.bookstore.DTO.Res_Get_Review;
import com.bookstore.Entity.OrderItem;
import com.bookstore.Entity.Review;
import com.bookstore.Entity.User;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.OrderItemRepository;
import com.bookstore.Repository.ReviewRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "highRatingBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> addReview(String authorizationHeader, String bookId, Req_Create_Review review) {
        try {
            String accessToken = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);

            List<OrderItem> orderItems = orderItemRepository.findOrderItemByBookIdAndStatus(bookId, OrderStatus.DELIVERED);

            List<Review> reviews = reviewRepository.findReviewsByBookIdOrderedByCreatedAtDesc(bookId);
            if (orderItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .message("You must purchase the book in order to leave a review!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            LocalDate reviewCreateDeadline = orderItems.get(0).getOrders().getOrderAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            reviewCreateDeadline = reviewCreateDeadline.plusMonths(1);

            if (reviewCreateDeadline.isBefore(LocalDate.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .message("It has been over a month since you purchased this book. Please purchase it again in order to leave a review!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (!reviews.isEmpty() && reviews.get(0).getCreatedAt().isAfter(orderItems.get(0).getOrders().getOrderAt())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .message("You have already reviewed this book. Please purchase it again to submit another review!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            Review new_review = new Review();
            assert (bookRepository.findById(bookId).isPresent());
            new_review.setBook(bookRepository.findById(bookId).get());
            assert (userRepository.findById(userId).isPresent());
            new_review.setUser(userRepository.findById(userId).get());
            new_review.setContent(review.getContent());
            new_review.setRating(review.getRating());

            Review temp = reviewRepository.save(new_review);
            Res_Get_Review res = new Res_Get_Review();
            res.convert(temp);
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Review created successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(res)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to create review, message =  " + ex.getMessage())
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
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all reviews successfully!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve all reviews, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "highRatingBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> update(String reviewId, String token, Req_Create_Review review) {
        try {
            String accessToken = token.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("User not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Optional<Review> ele = reviewRepository.findById(reviewId);

            if (ele.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Review not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (ele.get().getUser() != user.get()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("This review does not belong to the user!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            LocalDate reviewUpdateDeadline = ele.get().getCreatedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            reviewUpdateDeadline = reviewUpdateDeadline.plusMonths(1);

            if (reviewUpdateDeadline.isBefore(LocalDate.now())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("You cannot edit a review after 30 days from the date it was created!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            if (review.getContent() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("The Content must not be null!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            ele.get().setContent(review.getContent());
            ele.get().setRating(review.getRating());
            Res_Get_Review res = new Res_Get_Review();
            Review temp = reviewRepository.save(ele.get());
            res.convert(temp);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Review updated successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(res)
                    .success(true)
                    .build());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to update review, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
