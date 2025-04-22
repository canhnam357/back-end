package com.bookstore.Controller.General;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.DistributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distributors")
public class DistributorController {

    @Autowired
    private DistributorService distributorService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "") String keyword) {
        return distributorService.getAllNotPageable(keyword);
    }
}
