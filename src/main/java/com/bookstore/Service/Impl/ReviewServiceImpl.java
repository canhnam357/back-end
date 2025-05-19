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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrderItemRepository orderItemRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    @Caching(evict = {
            @CacheEvict(value = "highRatingBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> addReview(String authorizationHeader, String bookId, Req_Create_Review review) {
        try {
            log.info("Bắt đầu thêm đánh giá!");
            String accessToken = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);

            List<OrderItem> orderItems = orderItemRepository.findOrderItemByBookIdAndStatus(userId, bookId, OrderStatus.DELIVERED);

            List<Review> reviews = reviewRepository.findReviewsByBookIdOrderedByCreatedAtDesc(userId, bookId);
            if (orderItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .message("Vui lòng mua sách này để thực hiện đánh giá!")
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
                        .message("Đã được 1 tháng từ khi bạn mua sách này. Vui lòng mua lại để đánh giá!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (!reviews.isEmpty() && reviews.get(0).getCreatedAt().isAfter(orderItems.get(0).getOrders().getOrderAt())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .message("Bạn đã đánh giá sách này rồi. Vui lòng mua lại để đánh giá tiếp!")
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
            log.info("Thêm đánh giá thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo đánh giá mới thành công!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(res)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Thêm đánh giá thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
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
                    .message("Lấy danh sách đánh giá thành công!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách đánh giá thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "highRatingBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> update(String reviewId, String token, Req_Create_Review review) {
        try {
            log.info("Bắt đầu cập nhật đánh giá!");
            String accessToken = token.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Người dùng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Optional<Review> ele = reviewRepository.findById(reviewId);

            if (ele.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Đánh giá!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (ele.get().getUser() != user.get()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Đánh giá này không phải của bạn!")
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
                        .message("Bạn không thể sửa đánh giá sau 30 ngày từ khi nó được tạo!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            if (review.getContent() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Nội dung không được để trống!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            ele.get().setContent(review.getContent());
            ele.get().setRating(review.getRating());
            Res_Get_Review res = new Res_Get_Review();
            Review temp = reviewRepository.save(ele.get());
            res.convert(temp);
            log.info("Cập nhật đánh giá thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật đánh giá thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(res)
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Cập nhật đánh giá thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
