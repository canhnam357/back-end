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
public class Req_Add_Cart {
    private String bookId;

    @Positive(message = "Quantity must greater than 0")
    private int quantity;
}

// Req_Add_Cart