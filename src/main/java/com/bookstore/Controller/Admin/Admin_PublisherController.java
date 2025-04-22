package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/publishers")
public class Admin_PublisherController {

    @Autowired
    private PublisherService publisherService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return publisherService.search(page, size, keyword);
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createPublisher (@RequestBody Admin_Req_Create_Publisher createPublisher)  {
        return publisherService.create(createPublisher);
    }

    @DeleteMapping("")
    public ResponseEntity<GenericResponse> delete(@RequestParam String publisherId) {
        return publisherService.delete(publisherId);
    }
}
