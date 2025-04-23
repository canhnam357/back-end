package com.bookstore.Controller.General;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "") String keyword) {
        return authorService.getAllNotPageable(keyword);
    }


}
