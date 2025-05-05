package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.Admin_Req_Update_Distributor;
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

    @GetMapping("") // OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") String keyword) {
        System.out.println("ADMIN get all Distributor");
        return distributorService.search(index, size, keyword);
    }

    @PostMapping("") // OK
    public ResponseEntity<GenericResponse> createDistributor (@RequestBody Admin_Req_Create_Distributor createDistributor)  {
        System.out.println("ADMIN create Distributor");
        return distributorService.create(createDistributor);
    }

    @DeleteMapping("") // not used
    public ResponseEntity<GenericResponse> deleteDistributor (@RequestParam String distributorId) {
        return distributorService.delete(distributorId);
    }

    @PutMapping("/{distributorId}") // OK
    public ResponseEntity<GenericResponse> updateDistributor (@PathVariable String distributorId, @RequestBody Admin_Req_Update_Distributor distributor) {
        return distributorService.update(distributorId, distributor);
    }
}
