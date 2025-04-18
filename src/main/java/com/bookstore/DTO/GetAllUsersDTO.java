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
public class GetAllUsersDTO {
    String userId;
    String fullName;
    String email;
    String phoneNumber;
    Date dateOfBirth;
    String gender;
    boolean isActive;
    boolean isVerified;
}
