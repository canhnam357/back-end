package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Create_Discount;
import com.bookstore.DTO.Admin_Update_Discount;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/discounts")
public class Admin_DiscountController {
    @Autowired
    private DiscountService discountService;

    @PostMapping("")
    public ResponseEntity<GenericResponse> createDiscount(@RequestBody Admin_Create_Discount discount) {
        return discountService.createDiscount(discount);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "1") int index, @RequestParam(defaultValue = "10") int size) {
        return discountService.getAll(index, size);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<GenericResponse> getDiscountOfBook(@PathVariable String bookId) {
        return discountService.getDiscountOfBook(bookId);
    }

    @PutMapping("/{discountId}")
    public ResponseEntity<GenericResponse> updateDiscount(@PathVariable String discountId, @RequestBody Admin_Update_Discount discount) {
        return discountService.updateDiscount(discountId, discount);
    }
}
