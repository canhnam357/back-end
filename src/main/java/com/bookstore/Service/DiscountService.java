package com.bookstore.Service;


import com.bookstore.DTO.Admin_Create_Discount;
import com.bookstore.DTO.Admin_Update_Discount;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface DiscountService {
    ResponseEntity<GenericResponse> createDiscount(Admin_Create_Discount discount);

    ResponseEntity<GenericResponse> getAll(int index, int size);

    ResponseEntity<GenericResponse> updateDiscount(String discountId, Admin_Update_Discount discount);

    ResponseEntity<GenericResponse> getDiscountOfBook(String bookId);
}
