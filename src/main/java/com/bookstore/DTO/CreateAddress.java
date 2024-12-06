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
public class CreateAddress {
    @NotNull
    String fullName;

    // checked?
    @NotNull
    String phoneNumber;

    @NotNull
    String addressInformation;

    // Optional
    String otherDetail;

}
