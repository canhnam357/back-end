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
public class Res_PaymentInfo {
    BigDecimal amount;
    String BankCode;
    String payDate;
}
