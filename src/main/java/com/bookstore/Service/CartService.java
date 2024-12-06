package com.bookstore.Service;

import com.bookstore.DTO.AddToCart;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface CartService {

    ResponseEntity<GenericResponse> getCart(String userId);

    ResponseEntity<GenericResponse> addToCart(AddToCart addToCart, String userId);
}
