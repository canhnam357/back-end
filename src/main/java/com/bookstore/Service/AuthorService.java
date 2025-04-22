package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Author;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Admin_Req_Update_Author;
import org.springframework.http.ResponseEntity;

public interface AuthorService {
    ResponseEntity<GenericResponse> create(Admin_Req_Create_Author createAuthor);

    ResponseEntity<GenericResponse> getAll(int page, int size);

    ResponseEntity<GenericResponse> search(int page, int size, String keyword);

    ResponseEntity<GenericResponse> update(String authorId, Admin_Req_Update_Author updateAuthor);

    ResponseEntity<GenericResponse> delete(String authorId);

    ResponseEntity<GenericResponse> getAllNotPageable(String keyword);
}
