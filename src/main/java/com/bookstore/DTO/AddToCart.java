package com.bookstore.DTO;


import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCart {
    String bookId;

    @Positive(message = "Quantity must greater than 0")
    int quantity;
}
