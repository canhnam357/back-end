package com.bookstore.Service.Impl;

import com.bookstore.Constant.DiscountType;
import com.bookstore.DTO.Req_Add_Cart;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Get_Cart;
import com.bookstore.DTO.Req_Get_CartItem;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.Cart;
import com.bookstore.Entity.CartItem;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.CartItemRepository;
import com.bookstore.Repository.CartRepository;
import com.bookstore.Service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;

    @Override
    public ResponseEntity<GenericResponse> getCart(String userId) {
        try {
            // Lấy Cart theo userId
            Cart cart = cartRepository.findByUserUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

            // Lấy danh sách CartItem hiện tại
            List<CartItem> cartItems = cartItemRepository.findAllByCartUserUserId(userId);
            List<Req_Get_CartItem> cartItemList = new ArrayList<>();
            List<CartItem> newCartItems = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            // Xử lý từng CartItem
            for (CartItem cartItem : cartItems) {
                // Kiểm tra và cập nhật quantity nếu vượt quá inStock
                if (cartItem.getQuantity() > cartItem.getBook().getInStock()) {
                    cartItem.setQuantity(cartItem.getBook().getInStock());
                }

                // Bỏ qua CartItem có quantity == 0
                if (cartItem.getQuantity() == 0 || cartItem.getBook().isDeleted()) {
                    continue;
                }

                // Tính lại totalPrice cho CartItem
                cartItem.reCalTotalPrice();

                // Liên kết CartItem với Cart
                cartItem.setCart(cart);

                // Thêm vào danh sách mới
                newCartItems.add(cartItem);
                BigDecimal priceAfterSale = null;
                Book book = cartItem.getBook();
                BigDecimal price = cartItem.getTotalPrice();
                if (book.getDiscount() != null && book.getDiscount().getStartDate().isBefore(now) && book.getDiscount().getEndDate().isAfter(now)) {
                    if (book.getDiscount().getDiscountType() == DiscountType.FIXED) {
                        priceAfterSale = book.getPrice().subtract(book.getDiscount().getDiscount());
                    }
                    else {
                        priceAfterSale = book.getPrice()
                                .multiply(BigDecimal.valueOf(100L).subtract(book.getDiscount().getDiscount()))
                                .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);                    }
                    price = priceAfterSale.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                }

                // Tạo Req_Get_CartItem để trả về response
                cartItemList.add(new Req_Get_CartItem(
                        cartItem.getBook().getBookId(),
                        cartItem.getBook().getBookName(),
                        cartItem.getBook().getUrlThumbnail(),
                        cartItem.getBook().getPrice(),
                        priceAfterSale,
                        cartItem.getQuantity(),
                        price
                ));

                // Cộng dồn totalPrice
                totalPrice = totalPrice.add(cartItem.getTotalPrice());
            }

            // Xóa các CartItem cũ trong Cart (Hibernate sẽ xóa bản ghi nhờ orphanRemoval = true)
            cart.getCartItems().clear();

            // Thêm danh sách CartItem mới vào Cart
            cart.getCartItems().addAll(newCartItems);

            // Lưu Cart vào cơ sở dữ liệu
            cartRepository.save(cart);

            // Tạo response
            Req_Get_Cart res = new Req_Get_Cart(cartItemList);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy thông tin giỏ hàng thành công!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy thông tin giỏ hàng thất bại : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> addToCart(Req_Add_Cart addToCart, String userId) {
        try {
            log.info("Bắt đầu thêm vào giỏ hàng!");
            Optional<CartItem> tempcartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(addToCart.getBookId(), userId);
            CartItem _cartItem;
            if (tempcartItem.isEmpty())
            {
                _cartItem = new CartItem();
                assert (bookRepository.findById(addToCart.getBookId()).isPresent());
                _cartItem.setBook(bookRepository.findById(addToCart.getBookId()).get());
                assert (cartRepository.findByUserUserId(userId).isPresent());
                _cartItem.setCart(cartRepository.findByUserUserId(userId).get());
                _cartItem.setQuantity(addToCart.getQuantity());
            }
            else {
                _cartItem = tempcartItem.get();
                _cartItem.setQuantity(_cartItem.getQuantity() + addToCart.getQuantity());
            }
            _cartItem.reCalTotalPrice();
            if (_cartItem.getQuantity() <= 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Số lượng sản phẩm phải > 0!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            if (_cartItem.getQuantity() > _cartItem.getBook().getInStock()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Số lượng sản phẩm trong giỏ hàng không được nhiều hơn số lượng tồn kho!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            CartItem cartItem = cartItemRepository.save(_cartItem);
            log.info("Thêm vào giỏ hàng thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Thêm sản phẩm vào giỏ hàng thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Req_Get_CartItem(
                            cartItem.getBook().getBookId(),
                            cartItem.getBook().getBookName(),
                            cartItem.getBook().getUrlThumbnail(),
                            cartItem.getBook().getPrice(),
                            null,
                            cartItem.getQuantity(),
                            cartItem.getTotalPrice()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.info("Thêm vào giỏ hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> removeFromCart(String bookId, String userId) {
        try {
            log.info("Bắt đầu xoá khỏi giỏ hàng!");
            Optional<CartItem> cartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (cartItem.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy sản phẩm trong giỏ hàng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            cartItemRepository.delete(cartItem.get());
            log.info("Xoá khỏi giỏ hàng thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Xoá sản phẩm khỏi giỏ hàng thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Xoá khỏi giỏ hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> changeQuantity(String bookId, String userId, int quantity) {
        try {
            log.info("Bắt đầu thay đổi số lượng sản phẩm trong giỏ hàng!");
            Optional<CartItem> tempcartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (tempcartItem.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy sản phẩm trong giỏ hàng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (tempcartItem.get().getQuantity() + quantity <= 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không để thay đổi số lượng sản phẩm <= 0!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            tempcartItem.get().setQuantity(Math.min(tempcartItem.get().getQuantity() + quantity, tempcartItem.get().getBook().getInStock()));
            tempcartItem.get().reCalTotalPrice();
            CartItem cartItem = cartItemRepository.save(tempcartItem.get());
            BigDecimal priceAfterSale = null;
            Book book = cartItem.getBook();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            BigDecimal price = cartItem.getTotalPrice();
            if (book.getDiscount() != null && book.getDiscount().getStartDate().isBefore(now) && book.getDiscount().getEndDate().isAfter(now)) {
                if (book.getDiscount().getDiscountType() == DiscountType.FIXED) {
                    priceAfterSale = book.getPrice().subtract(book.getDiscount().getDiscount());
                }
                else {
                    priceAfterSale = book.getPrice()
                            .multiply(BigDecimal.valueOf(100L).subtract(book.getDiscount().getDiscount()))
                            .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);                }
                price = priceAfterSale.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            }
            log.info("Thay đổi số lượng sản phẩm trong giỏ hàng thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Thay đổi số lượng sản phẩm trong giỏ hàng thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Req_Get_CartItem(
                            cartItem.getBook().getBookId(),
                            cartItem.getBook().getBookName(),
                            cartItem.getBook().getUrlThumbnail(),
                            cartItem.getBook().getPrice(),
                            priceAfterSale,
                            cartItem.getQuantity(),
                            price
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Thay đổi số lượng sản phẩm trong giỏ hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> updateQuantity(String bookId, String userId, int quantity) {
        try {
            log.info("Bắt đầu cập nhật số lượng sản phẩm trong giỏ hàng!");
            Optional<CartItem> tempCartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (tempCartItem.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy sản phẩm trong giỏ hàng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (quantity <= 0 || tempCartItem.get().getBook().getInStock() == 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không để cập nhật số lượng sản phẩm <= 0!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            tempCartItem.get().setQuantity(Math.min(quantity, tempCartItem.get().getBook().getInStock()));
            tempCartItem.get().reCalTotalPrice();
            CartItem cartItem = cartItemRepository.save(tempCartItem.get());
            BigDecimal priceAfterSale = null;
            Book book = cartItem.getBook();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            BigDecimal price = cartItem.getTotalPrice();
            if (book.getDiscount() != null && book.getDiscount().getStartDate().isBefore(now) && book.getDiscount().getEndDate().isAfter(now)) {
                if (book.getDiscount().getDiscountType() == DiscountType.FIXED) {
                    priceAfterSale = book.getPrice().subtract(book.getDiscount().getDiscount());
                }
                else {
                    priceAfterSale = book.getPrice()
                            .multiply(BigDecimal.valueOf(100L).subtract(book.getDiscount().getDiscount()))
                            .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);                }
                price = priceAfterSale.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            }
            log.info("Cập nhật số lượng sản phẩm trong giỏ hàng thành công!");
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Cập nhật số lượng sản phẩm trong giỏ hàng thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Req_Get_CartItem(
                            cartItem.getBook().getBookId(),
                            cartItem.getBook().getBookName(),
                            cartItem.getBook().getUrlThumbnail(),
                            cartItem.getBook().getPrice(),
                            priceAfterSale,
                            cartItem.getQuantity(),
                            price
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Cập nhật số lượng sản phẩm trong giỏ hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }


}
