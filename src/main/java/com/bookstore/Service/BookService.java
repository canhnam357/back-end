package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.Admin_Req_Update_Book;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface BookService {

    ResponseEntity<GenericResponse> getAll(int page, int size, String keyword);

    ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size, BigDecimal leftBound, BigDecimal rightBound, String authorId, String publisherId, String distributorId, String bookName, String sort, String categoryIds);

    ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId);

    ResponseEntity<GenericResponse> getNewArrivalsBook(int page, int size); // get Book such newArrival = true and isDeleted = false

    ResponseEntity<GenericResponse> create(Admin_Req_Create_Book createBook);

    ResponseEntity<GenericResponse> upload(MultipartFile file, String bookId, int isThumbnail);

    ResponseEntity<GenericResponse> adminGetBooksOfAuthor(int page, int size, String authorId);

    ResponseEntity<GenericResponse> getPriceRange();

    ResponseEntity<GenericResponse> delete(String bookId);

    ResponseEntity<GenericResponse> update(String bookId, Admin_Req_Update_Book bookDto);
}
