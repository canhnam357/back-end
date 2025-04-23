package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Req_Get_CartItem {
    private String bookId;
    private String bookName;
    private String urlThumbnail;
    private BigDecimal price;
    private int quantity;
    private BigDecimal totalPrice;
}
