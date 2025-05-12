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
public class Res_Get_OrderDetail {
    private String orderDetailId;
    private String bookId;
    private String bookName;
    private BigDecimal price;
    private BigDecimal priceAfterSales;
    private int quantity;
    private BigDecimal totalPrice;
    private String urlThumbnail;
}
