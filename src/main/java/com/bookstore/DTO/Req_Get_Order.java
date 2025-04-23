package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Req_Get_Order {
    private String orderId;
    private String orderStatus;
    private String paymentMethod;
    private List<Req_Get_OrderItem> orderItems;
    private String address;
    private Date orderAt;
    private BigDecimal totalPrice;
}
