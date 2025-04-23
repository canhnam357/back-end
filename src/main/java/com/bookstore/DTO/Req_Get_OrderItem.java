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
public class Req_Get_OrderItem {
    private String orderItemId;
    private String bookName;
    private int quantity;
    private BigDecimal totalPrice;
    private String urlThumbnail;
}
