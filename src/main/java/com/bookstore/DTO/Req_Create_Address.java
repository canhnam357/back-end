package com.bookstore.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Req_Create_Address {
    @NotNull
    private String fullName;

    // checked?
    @NotNull
    private String phoneNumber;

    @NotNull
    private String addressInformation;

    // Optional
    private String otherDetail;

}
