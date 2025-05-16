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
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER', 'SHIPPER')")
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return addressService.getAll(userId);
    }

    @PostMapping("")
    public ResponseEntity<GenericResponse> create(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody Req_Create_Address createAddress) {
        System.err.println("ORTHER DETAIL " + createAddress.getOtherDetail());
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

    @PatchMapping("/{addressId}")
    public ResponseEntity<GenericResponse> update(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody Req_PatchUpdate_Address address,
                                                  @PathVariable String addressId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println(userId);
        System.err.println("ORTHER DETAIL " + address.getOtherDetail());
        return addressService.update(address, userId, addressId);
    }

    @PostMapping("/{addressId}/set-default")
    public ResponseEntity<GenericResponse> setDefault(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String addressId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return addressService.setDefault(userId, addressId);
    }

}
