package com.bookstore.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Login {

    @NotBlank
    @NotEmpty
    private String email;

    @NotBlank
    @NotEmpty
    private String password;

}