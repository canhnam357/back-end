package com.bookstore.Service;

import com.bookstore.DTO.CreateBook;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Book;
import org.springframework.http.ResponseEntity;

public interface BookService {

    ResponseEntity<GenericResponse> getAll(int page, int size);

    ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size);

    ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId);

    ResponseEntity<GenericResponse> create(CreateBook createBook);
}