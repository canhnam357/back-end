package com.bookstore.Controller.User;


import com.bookstore.DTO.AddToCart;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER')")
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getCart(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return cartService.getCart(userId);
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<GenericResponse> addToCart(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody AddToCart addToCart) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return cartService.addToCart(addToCart, userId);
    }
}
