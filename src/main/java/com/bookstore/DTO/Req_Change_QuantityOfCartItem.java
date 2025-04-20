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
public class Req_Change_QuantityOfCartItem {
    private String bookId;
    private int quantity;
}
