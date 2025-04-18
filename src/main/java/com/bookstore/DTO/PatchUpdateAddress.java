package com.bookstore.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL) // Chỉ bao gồm các trường không null khi PATCH
@Getter
@Setter

public class PatchUpdateAddress {

    String addressId;

    String fullName;

    // checked?
    String phoneNumber;

    String addressInformation;

    // Optional
    String otherDetail;
}