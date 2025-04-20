package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    ResponseEntity<GenericResponse> getAll(int page, int size);

    ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size);

    ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId);

    ResponseEntity<GenericResponse> getNewArrivalsBook(int page, int size); // get Book such newArrival = true and isDeleted = false

    ResponseEntity<GenericResponse> create(Admin_Req_Create_Book createBook);

    ResponseEntity<GenericResponse> upload(MultipartFile file, String bookId, int isThumbnail);

    ResponseEntity<GenericResponse> adminGetBooksOfAuthor(int page, int size, String authorId);
}
