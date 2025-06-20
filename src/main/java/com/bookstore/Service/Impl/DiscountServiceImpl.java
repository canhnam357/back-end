package com.bookstore.Service.Impl;

import com.bookstore.Constant.DiscountType;
import com.bookstore.DTO.Admin_Create_Discount;
import com.bookstore.DTO.Admin_Update_Discount;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.Discount;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.DiscountRepository;
import com.bookstore.Service.DiscountService;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Caching(evict = {
            @CacheEvict(value = "discountBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> createDiscount(Admin_Create_Discount discount) {
        try {
            log.info("Bắt đầu tạo khuyến mãi!");
            if (discount.getStartDate() == null || discount.getEndDate() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Thời gian bắt đầu và thời gian kết thúc không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            if (discount.getEndDate().isBefore(discount.getStartDate())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Thời gian bắt đầu phải đứng trước thời gian kết thúc!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            if (!discount.getDiscountType().equals(DiscountType.FIXED.name()) &&
                    !discount.getDiscountType().equals(DiscountType.PERCENTAGE.name())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không tìm thấy Loại khuyến mãi!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            if (discount.getBookId() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("BookId không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            Optional<Book> bookOpt = bookRepository.findById(discount.getBookId());
            if (bookOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không tìm thấy Sách!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            Book book = bookOpt.get();

            if (discount.getDiscount() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Số tiền hoặc phần trăm không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            BigDecimal price = book.getPrice();

            if (discount.getDiscountType().equals(DiscountType.FIXED.name())) {
                price = price.subtract(discount.getDiscount());
            } else {
                price = price.multiply(BigDecimal.valueOf(100L).subtract(discount.getDiscount()));
                price = price.divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(book.getPrice()) >= 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Giá sách sau khi khuyến mãi phải > 0 và nhỏ hơn giá hiện tại!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            // Xóa discount cũ nếu có
            Optional<Discount> existingDiscount = discountRepository.findByBook(book);
            if (existingDiscount.isPresent()) {
                // Gỡ liên kết từ book sang discount cũ
                book.setDiscount(null);
                bookRepository.save(book);

                // Xóa discount cũ khỏi database
                discountRepository.delete(existingDiscount.get());
            }


            // Tạo và lưu discount mới
            Discount ele = new Discount();
            ele.setStartDate(discount.getStartDate());
            ele.setEndDate(discount.getEndDate());
            ele.setDiscountType(DiscountType.valueOf(discount.getDiscountType()));
            ele.setBook(book); // liên kết 1 chiều từ Discount sang Book
            ele.setDiscount(discount.getDiscount());
            ele.setActive(discount.getIsActive());
            ele = discountRepository.save(ele);

            // Liên kết ngược lại từ Book sang Discount
            book.setDiscount(ele);
            bookRepository.save(book);
            log.info("Tạo khuyến mãi thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo khuyến mãi thành công!")
                    .result(ele)
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Tạo khuyến mãi thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to create discount, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getAll(int index, int size) {
        try {
            Page<Discount> discountPage = discountRepository.getAll(PageRequest.of(index - 1, size));
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all discounts successfully!")
                    .result(discountPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách khuyến mãi thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve all discounts, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Caching(evict = {
            @CacheEvict(value = "discountBooks", allEntries = true)
    })
    public ResponseEntity<GenericResponse> updateDiscount(String discountId, Admin_Update_Discount discount) {
        try {
            log.info("Bắt đầu cập nhật khuyến mãi!");
            if (discount.getStartDate() == null || discount.getEndDate() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Start date and end date must be provided!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            if (discount.getEndDate().isBefore(discount.getStartDate())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Start date must be before end date!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            if (!discount.getDiscountType().equals(DiscountType.FIXED.name()) &&
                    !discount.getDiscountType().equals(DiscountType.PERCENTAGE.name())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Discount type not found!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            if (discountRepository.findById(discountId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Discount not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }


            if (discount.getDiscount() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Amount or percentage must be provided!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            Discount ele = discountRepository.findById(discountId).get();

            Book book = ele.getBook();

            BigDecimal price = book.getPrice();

            if (discount.getDiscountType().equals(DiscountType.FIXED.name())) {
                price = price.subtract(discount.getDiscount());
            } else {
                price = price.multiply(BigDecimal.valueOf(100L).subtract(discount.getDiscount()));
                price = price.divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(book.getPrice()) >= 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Price after sales must be greater than 0 and less than the current price!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }



            // Tạo và lưu discount mới
            ele.setStartDate(discount.getStartDate());
            ele.setEndDate(discount.getEndDate());
            ele.setDiscountType(DiscountType.valueOf(discount.getDiscountType()));
            ele.setDiscount(discount.getDiscount());
            ele.setActive(discount.getIsActive());
            discountRepository.save(ele);
            bookRepository.save(book);
            log.info("Cập nhật khuyến mãi thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Discount updated successfully!")
                    .result(ele)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật khuyến mãi thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to update discount, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getDiscountOfBook(String bookId) {
        try {

            if (bookRepository.findById(bookId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Book not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Optional<Discount> discount = discountRepository.findByBook(bookRepository.findById(bookId).get());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved discount of book successfully!")
                    .result(discount.orElse(null))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách khuyến mãi của sách thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve discount of book, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
