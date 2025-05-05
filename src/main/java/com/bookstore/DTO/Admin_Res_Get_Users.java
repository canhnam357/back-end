package com.bookstore.DTO;

import com.bookstore.Constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin_Res_Get_Users {
    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Date dateOfBirth;
    private String gender;
    private boolean isActive;
    private boolean isVerified;
    private String role;
}
