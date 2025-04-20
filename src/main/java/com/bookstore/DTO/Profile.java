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
    private String fullName;
    private String phoneNumber;
    private String email;
    private List<Res_Get_Address> addressList;

    public void addAddress(Res_Get_Address resGetAddress) {
        addressList.add(resGetAddress);
    }
}
