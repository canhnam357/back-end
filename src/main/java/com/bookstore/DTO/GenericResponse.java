package com.bookstore.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericResponse {
    private Boolean success;
    private String message;
    private Object result;
    private int statusCode;
    private String redirectUrl;
}
