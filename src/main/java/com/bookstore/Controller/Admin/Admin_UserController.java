package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_UpdateUserDTO;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/users")
public class Admin_UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "2") int isActive,
                                                   @RequestParam(defaultValue = "2") int isVerified,
                                                   @RequestParam(defaultValue = "") String email) {
        return userService.getAll(page, size, isActive, isVerified, email);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GenericResponse> updateUserStatus(@PathVariable String userId, @RequestBody Admin_UpdateUserDTO adminUpdateUserDTO) {
        return userService.updateUserStatus(userId, adminUpdateUserDTO);
    }
}
