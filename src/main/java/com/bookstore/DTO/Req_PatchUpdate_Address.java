package com.bookstore.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter

public class Req_PatchUpdate_Address {

    private String fullName;

    // checked?
    private String phoneNumber;

    private String addressInformation;

    // Optional
    private String otherDetail;
}