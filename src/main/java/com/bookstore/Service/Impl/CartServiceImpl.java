package com.bookstore.Service.Impl;

import com.bookstore.DTO.Req_Add_Cart;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Get_Cart;
import com.bookstore.DTO.Req_Get_CartItem;
import com.bookstore.Entity.Cart;
import com.bookstore.Entity.CartItem;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.CartItemRepository;
import com.bookstore.Repository.CartRepository;
import com.bookstore.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

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

            // Xử lý từng CartItem
            for (CartItem cartItem : cartItems) {
                // Kiểm tra và cập nhật quantity nếu vượt quá inStock
                if (cartItem.getQuantity() > cartItem.getBook().getInStock()) {
                    cartItem.setQuantity(cartItem.getBook().getInStock());
                }

                // Bỏ qua CartItem có quantity == 0
                if (cartItem.getQuantity() == 0) {
                    continue;
                }

                // Tính lại totalPrice cho CartItem
                cartItem.reCalTotalPrice();

                // Liên kết CartItem với Cart
                cartItem.setCart(cart);

                // Thêm vào danh sách mới
                newCartItems.add(cartItem);

                // Tạo Req_Get_CartItem để trả về response
                cartItemList.add(new Req_Get_CartItem(
                        cartItem.getBook().getBookId(),
                        cartItem.getBook().getBookName(),
                        cartItem.getBook().getUrlThumbnail(),
                        cartItem.getBook().getPrice(),
                        cartItem.getQuantity(),
                        cartItem.getTotalPrice()
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

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get Cart Successfully!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get Cart failed: " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> addToCart(Req_Add_Cart addToCart, String userId) {
        try {
            Optional<CartItem> tempcartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(addToCart.getBookId(), userId);
            CartItem _cartItem;
            if (!tempcartItem.isPresent())
            {
                _cartItem = new CartItem();
                _cartItem.setBook(bookRepository.findById(addToCart.getBookId()).get());
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
                        .message("Quantity must greater than 0!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            if (_cartItem.getQuantity() > _cartItem.getBook().getInStock()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Quantity less than or equal inStock!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            CartItem cartItem = cartItemRepository.save(_cartItem);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Add to Cart success!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Req_Get_CartItem(
                            cartItem.getBook().getBookId(),
                            cartItem.getBook().getBookName(),
                            cartItem.getBook().getUrlThumbnail(),
                            cartItem.getBook().getPrice(),
                            cartItem.getQuantity(),
                            cartItem.getTotalPrice()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Add to Cart failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> removeFromCart(String bookId, String userId) {
        try {
            Optional<CartItem> cartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (!cartItem.isPresent()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found cartItem!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            cartItemRepository.delete(cartItem.get());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Remove from Cart success!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Remove from Cart failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> changeQuantity(String bookId, String userId, int quantity) {
        try {
            Optional<CartItem> tempcartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (!tempcartItem.isPresent()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found cartItem!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (tempcartItem.get().getQuantity() + quantity <= 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Can't make quantity <= 0!!!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            tempcartItem.get().setQuantity(Math.min(tempcartItem.get().getQuantity() + quantity, tempcartItem.get().getBook().getInStock()));
            tempcartItem.get().reCalTotalPrice();
            CartItem cartItem = cartItemRepository.save(tempcartItem.get());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Change quantity success!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Req_Get_CartItem(
                            cartItem.getBook().getBookId(),
                            cartItem.getBook().getBookName(),
                            cartItem.getBook().getUrlThumbnail(),
                            cartItem.getBook().getPrice(),
                            cartItem.getQuantity(),
                            cartItem.getTotalPrice()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Change quantity cart-item failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> updateQuantity(String bookId, String userId, int quantity) {
        try {
            Optional<CartItem> tempcartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (!tempcartItem.isPresent()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found cartItem!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (quantity <= 0 || tempcartItem.get().getBook().getInStock() == 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Can't make quantity <= 0!!!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }
            tempcartItem.get().setQuantity(Math.min(quantity, tempcartItem.get().getBook().getInStock()));
            tempcartItem.get().reCalTotalPrice();
            CartItem cartItem = cartItemRepository.save(tempcartItem.get());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Update quantity success!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Req_Get_CartItem(
                            cartItem.getBook().getBookId(),
                            cartItem.getBook().getBookName(),
                            cartItem.getBook().getUrlThumbnail(),
                            cartItem.getBook().getPrice(),
                            cartItem.getQuantity(),
                            cartItem.getTotalPrice()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update quantity cart-item failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }


}
