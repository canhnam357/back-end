package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_BookType;
import com.bookstore.DTO.Admin_Req_Update_BookType;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/book-types")
public class Admin_BookTypeController {

    @Autowired
    private BookTypeService bookTypeService;


    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        System.out.println("ADMIN get all BookType");
        return bookTypeService.getAll(page, size);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createBookType (@RequestBody Admin_Req_Create_BookType createBookType)  {
        System.out.println("ADMIN create BookType");
        return bookTypeService.create(createBookType);
    }

//    @DeleteMapping("")
//    public ResponseEntity<GenericResponse> deleteBookType (@RequestParam String bookTypeId) {
//        return bookTypeService.delete(bookTypeId);
//    }

    @PutMapping("/{bookTypeId}")
    public ResponseEntity<GenericResponse> updateBookType (@PathVariable String bookTypeId, @RequestBody Admin_Req_Update_BookType bookType) {
        return bookTypeService.update(bookTypeId, bookType);
    }
}
