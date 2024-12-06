package com.bookstore.Controller.Admin;


import com.bookstore.DTO.CreateAuthor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/authors")
public class Admin_AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return authorService.getAll(page, size);
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createAuthor (@RequestBody CreateAuthor createAuthor)  {
        return authorService.create(createAuthor);
    }
}
