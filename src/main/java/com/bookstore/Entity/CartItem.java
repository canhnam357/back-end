package com.bookstore.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cartItem")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cartItemId;

    @Positive(message = "Quantity must be greater than 0")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "cartId", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;

    @Column(precision = 12, scale = 3)
    private BigDecimal totalPrice;

    public void reCalTotalPrice() {
        totalPrice = book.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
