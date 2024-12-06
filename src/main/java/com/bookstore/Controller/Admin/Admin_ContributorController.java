package com.bookstore.Controller.Admin;

import com.bookstore.DTO.CreateContributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.ContributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/contributors")
public class Admin_ContributorController {

    @Autowired
    private ContributorService contributorService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return contributorService.getAll(page, size);
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createContributor (@RequestBody CreateContributor createContributor)  {
        return contributorService.create(createContributor);
    }
}
