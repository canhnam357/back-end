package com.bookstore.Controller.User;

import com.bookstore.DTO.Req_Create_Address;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_PatchUpdate_Address;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER')")
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return addressService.getAll(page, size, userId);
    }

    @PostMapping("")
    public ResponseEntity<GenericResponse> create(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody Req_Create_Address createAddress) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println(userId);
        return addressService.create(createAddress, userId);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<GenericResponse> delete(@RequestHeader("Authorization") String authorizationHeader,
                                                  @PathVariable String addressId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return addressService.delete(addressId, userId);
    }

    @PatchMapping("")
    public ResponseEntity<GenericResponse> update(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody Req_PatchUpdate_Address address) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println(userId);
        return addressService.update(address, userId);
    }

}
