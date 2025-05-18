package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/publishers")
@RequiredArgsConstructor
public class Admin_PublisherController {

    private final PublisherService publisherService;
    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return publisherService.search(index, size, keyword);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createPublisher (@RequestBody Admin_Req_Create_Publisher createPublisher)  {
        return publisherService.create(createPublisher);
    }

    @PutMapping("/{publisherId}") // OK
    public ResponseEntity<GenericResponse> updatePublisher (@PathVariable String publisherId, @RequestBody Admin_Req_Update_Publisher publisher) {
        return publisherService.update(publisherId, publisher);
    }
}
