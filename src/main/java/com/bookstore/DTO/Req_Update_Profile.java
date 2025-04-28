package com.bookstore.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Req_Update_Profile {
    private String fullName;
    private String phoneNumber;
    private String gender;
    private Date dateOfBirth;
}
