package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Book;
import com.bookstore.DTO.Admin_Req_Update_Book;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;

public interface BookService {

    ResponseEntity<GenericResponse> getAll(int page, int size, String keyword);

    ResponseEntity<GenericResponse> getAllBookNotDeleted(int page, int size, BigDecimal leftBound, BigDecimal rightBound, String authorId, String publisherId, String distributorId, String bookName, String sort, String categoryIds);

    ResponseEntity<GenericResponse> getByIdNotDeleted(String bookId);

    ResponseEntity<GenericResponse> getNewArrivalsBook(); // get Book such newArrival = true and isDeleted = false

    ResponseEntity<GenericResponse> create(Admin_Req_Create_Book createBook);


    ResponseEntity<GenericResponse> adminGetBooksOfAuthor(int page, int size, String authorId);

    ResponseEntity<GenericResponse> adminGetBooksOfPublisher(int page, int size, String publisherId);

    ResponseEntity<GenericResponse> adminGetBooksOfDistributor(int page, int size, String distributorId);

    ResponseEntity<GenericResponse> adminGetBooksOfCategory(int page, int size, String categoryId);

    ResponseEntity<GenericResponse> getPriceRange();

    ResponseEntity<GenericResponse> update(String bookId, Admin_Req_Update_Book bookDto);

    ResponseEntity<GenericResponse> search(String keyword);

    ResponseEntity<GenericResponse> getDiscountBook();

    ResponseEntity<GenericResponse> getHighRatingBook();

    ResponseEntity<GenericResponse> getMostPopularBooks();

    ResponseEntity<GenericResponse> getBooksInCategoriesMostSold();
}
