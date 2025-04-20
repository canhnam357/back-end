package com.bookstore.Service;

import com.bookstore.DTO.Req_Add_Cart;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface CartService {

    ResponseEntity<GenericResponse> getCart(String userId);

    ResponseEntity<GenericResponse> addToCart(Req_Add_Cart addToCart, String userId);

    ResponseEntity<GenericResponse> removeFromCart(String bookId, String userId);

    ResponseEntity<GenericResponse> changeQuantity(String bookId, String userId, int quantity);

    ResponseEntity<GenericResponse> updateQuantity(String bookId, String userId, int quantity);
}
