package com.bookstore.Controller.User;


import com.bookstore.DTO.Req_Add_Cart;
import com.bookstore.DTO.Req_Update_QuantityOfCartItem;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Change_QuantityOfCartItem;
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
        System.err.println(userId);
        System.err.println("Get cart");
        return cartService.getCart(userId);
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<GenericResponse> addToCart(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody Req_Add_Cart addToCart) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("add to cart" + userId);
        return cartService.addToCart(addToCart, userId);
    }

    @DeleteMapping("/{bookId}") // for delete button
    public ResponseEntity<GenericResponse> removeFromCart(@RequestHeader("Authorization") String authorizationHeader,
                                                          @PathVariable String bookId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("Remove from cart" + userId);
        return cartService.removeFromCart(bookId, userId);
    }

    @PostMapping("/change-quantity") // for +1 /-1 button
    public ResponseEntity<GenericResponse> changeQuantity(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody Req_Update_QuantityOfCartItem changeQuantity) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("Change quantity " + userId + " bookId " + changeQuantity.getBookId());
        return cartService.changeQuantity(changeQuantity.getBookId(), userId, changeQuantity.getQuantity());
    }

    @PostMapping("/update-quantity") // for quantity - text box
    public ResponseEntity<GenericResponse> updateQuantity(@RequestHeader("Authorization") String authorizationHeader,
                                                          @RequestBody Req_Change_QuantityOfCartItem updateQuantity) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("Update quantity " + userId + " bookId " + updateQuantity.getBookId());
        return cartService.updateQuantity(updateQuantity.getBookId(), userId, updateQuantity.getQuantity());
    }
}
