package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.Admin_Req_Update_Publisher;
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

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        System.out.println("ADMIN get all Publisher");
        return publisherService.search(page, size, keyword);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createPublisher (@RequestBody Admin_Req_Create_Publisher createPublisher)  {
        System.out.println("ADMIN create Publisher");
        return publisherService.create(createPublisher);
    }

    @DeleteMapping("") // not used
    public ResponseEntity<GenericResponse> deletePublisher (@RequestParam String publisherId) {
        System.out.println("ADMIN delete Publisher");
        return publisherService.delete(publisherId);
    }

    @PutMapping("/{publisherId}") // OK
    public ResponseEntity<GenericResponse> updatePublisher (@PathVariable String publisherId, @RequestBody Admin_Req_Update_Publisher publisher) {
        System.out.println("ADMIN update Publisher");
        return publisherService.update(publisherId, publisher);
    }
}
