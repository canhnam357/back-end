package com.bookstore.Controller.Admin;


import com.bookstore.DTO.Admin_Req_Create_Author;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Admin_Req_Update_Author;
import com.bookstore.Service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/authors")
@RequiredArgsConstructor
public class Admin_AuthorController {

    private final AuthorService authorService;

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return authorService.search(index, size, keyword);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createAuthor (@RequestBody Admin_Req_Create_Author createAuthor)  {
        return authorService.create(createAuthor);
    }

    @PutMapping("/{authorId}") // OK
    public ResponseEntity<GenericResponse> updateAuthor (@PathVariable String authorId, @RequestBody Admin_Req_Update_Author updateAuthor)  {
        return authorService.update(authorId, updateAuthor);
    }

    @DeleteMapping("/{authorId}") // OK
    public ResponseEntity<GenericResponse> deleteAuthor (@PathVariable String authorId) {
        return authorService.delete(authorId);
    }

}
