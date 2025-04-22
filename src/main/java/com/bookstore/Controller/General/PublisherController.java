package com.bookstore.Controller.General;

import com.bookstore.DTO.Admin_Req_Create_Publisher;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    @Autowired
    private PublisherService publisherService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "") String keyword) {
        return publisherService.getAllNotPageable(keyword);
    }
}
