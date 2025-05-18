package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.Admin_Req_Update_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.DistributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/distributors")
@RequiredArgsConstructor
public class Admin_DistributorController {

    private final DistributorService distributorService;

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return distributorService.search(index, size, keyword);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createDistributor (@RequestBody Admin_Req_Create_Distributor createDistributor)  {
        return distributorService.create(createDistributor);
    }

    @PutMapping("/{distributorId}") // OK
    public ResponseEntity<GenericResponse> updateDistributor (@PathVariable String distributorId, @RequestBody Admin_Req_Update_Distributor distributor) {
        return distributorService.update(distributorId, distributor);
    }
}
