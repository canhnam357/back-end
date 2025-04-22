package com.bookstore.Service.Impl;

import com.bookstore.DTO.Req_Add_Cart;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.CartItem;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.CartItemRepository;
import com.bookstore.Repository.CartRepository;
import com.bookstore.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
            List<CartItem> cartItems = cartItemRepository.findAllByCartUserUserId(userId);
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get Cart Successfully!")
                            .result(cartItems)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get Cart failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> addToCart(Req_Add_Cart addToCart, String userId) {
        try {
            Optional<CartItem> cartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(addToCart.getBookId(), userId);
            CartItem _cartItem;
            if (!cartItem.isPresent())
            {
                _cartItem = new CartItem();
                _cartItem.setBook(bookRepository.findById(addToCart.getBookId()).get());
                _cartItem.setCart(cartRepository.findByUserUserId(userId).get());
                _cartItem.setQuantity(addToCart.getQuantity());
            }
            else {
                _cartItem = cartItem.get();
                _cartItem.setQuantity(_cartItem.getQuantity() + addToCart.getQuantity());
            }
            _cartItem.reCalTotalPrice();;
            if (_cartItem.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(
                        GenericResponse.builder()
                                .message("Quantity must greater than 0!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build()
                );
            }
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Add to Cart successfully!")
                            .result(cartItemRepository.save(_cartItem))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Add to Cart failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> removeFromCart(String bookId, String userId) {
        try {
            Optional<CartItem> cartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (!cartItem.isPresent()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Not found cartItem!!!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }
            cartItemRepository.delete(cartItem.get());
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Remove from Cart successfully!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Remove from Cart failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> changeQuantity(String bookId, String userId, int quantity) {
        try {
            Optional<CartItem> cartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (!cartItem.isPresent()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Not found cartItem!!!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }
            System.out.println("BEFORE " + cartItem.get().getQuantity());
            if (cartItem.get().getQuantity() + quantity <= 0) {
                cartItemRepository.delete(cartItem.get());
            }
            else
            {
                cartItem.get().setQuantity(cartItem.get().getQuantity() + quantity);
                cartItemRepository.save(cartItem.get());
            }
            System.out.println("AFTER " + cartItem.get().getQuantity());
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Changed quantity cart-item successfully!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Change quantity cart-item failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> updateQuantity(String bookId, String userId, int quantity) {
        try {
            Optional<CartItem> cartItem = cartItemRepository.findByBookBookIdAndCartUserUserId(bookId, userId);
            if (!cartItem.isPresent()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Not found cartItem!!!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }

            if (quantity <= 0) {
                cartItemRepository.delete(cartItem.get());
            }
            else
            {
                cartItem.get().setQuantity(quantity);
                cartItemRepository.save(cartItem.get());
            }
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Updated quantity cart-item successfully!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Update quantity cart-item failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }


}
