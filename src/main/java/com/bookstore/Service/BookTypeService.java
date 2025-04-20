package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_BookType;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface BookTypeService {
    ResponseEntity<GenericResponse> create(Admin_Req_Create_BookType createBookType);

    ResponseEntity<GenericResponse> getAll(int page, int size);
}
