package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.DistributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/distributors")
public class Admin_DistributorController {

    @Autowired
    private DistributorService distributorService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return distributorService.getAll(page, size);
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createContributor (@RequestBody Admin_Req_Create_Distributor createDistributor)  {
        return distributorService.create(createDistributor);
    }
}
