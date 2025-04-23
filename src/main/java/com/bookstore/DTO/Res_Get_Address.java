package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Res_Get_Address {
    private String addressId;
    private String fullName;
    private String phoneNumber;
    private String addressInformation;
    private String otherDetail;
    private boolean isDefault;
}
