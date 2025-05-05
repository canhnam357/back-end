package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Req_Create_Order {
    private List<String> bookIds;
    private String addressId;
    private String paymentMethod;
}
