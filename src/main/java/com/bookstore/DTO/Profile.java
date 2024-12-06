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
public class Profile {
    String fullName;
    String phoneNumber;
    String email;
    List<AddressResponse> addressList;

    public void addAddress(AddressResponse addressResponse) {
        addressList.add(addressResponse);
    }
}
