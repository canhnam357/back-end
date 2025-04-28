package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Res_Get_Order {
    private String orderId;
    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;
    private String address;
    private String phoneNumber;
    private Date orderAt;
    private BigDecimal totalPrice;
}
