package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_BookType;
import com.bookstore.DTO.Admin_Req_Update_BookType;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/book-types")
@RequiredArgsConstructor
public class Admin_BookTypeController {

    private final BookTypeService bookTypeService;

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return bookTypeService.getAll(page, size);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createBookType (@RequestBody Admin_Req_Create_BookType createBookType)  {
        return bookTypeService.create(createBookType);
    }

    @PutMapping("/{bookTypeId}")
    public ResponseEntity<GenericResponse> updateBookType (@PathVariable String bookTypeId, @RequestBody Admin_Req_Update_BookType bookType) {
        return bookTypeService.update(bookTypeId, bookType);
    }
}
