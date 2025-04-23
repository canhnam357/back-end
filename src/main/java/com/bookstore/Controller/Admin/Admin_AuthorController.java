package com.bookstore.Controller.Admin;


import com.bookstore.DTO.Admin_Req_Create_Author;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Admin_Req_Update_Author;
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
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        System.out.println("ADMIN get All authors , keyword = " + "\"" + keyword + "\"");
        return authorService.search(page, size, keyword);
    }

    @PostMapping("")
    public ResponseEntity<GenericResponse> createAuthor (@RequestBody Admin_Req_Create_Author createAuthor)  {
        System.out.println("ADMIN Create Author");
        return authorService.create(createAuthor);
    }

    @PutMapping("/{authorId}")
    public ResponseEntity<GenericResponse> updateAuthor (@PathVariable String authorId, @RequestBody Admin_Req_Update_Author updateAuthor)  {
        System.out.println("ADMIN update Author");
        return authorService.update(authorId, updateAuthor);
    }

    @DeleteMapping("/{authorId}")
    public ResponseEntity<GenericResponse> deleteAuthor (@PathVariable String authorId) {
        System.out.println("ADMIN delete Author");
        return authorService.delete(authorId);
    }

}
