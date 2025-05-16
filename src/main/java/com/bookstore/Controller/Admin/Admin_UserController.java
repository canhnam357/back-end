package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Update_UserStatus;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class Admin_UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int index,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "2") int isActive,
                                                   @RequestParam(defaultValue = "2") int isVerified,
                                                   @RequestParam(defaultValue = "") String email) {
        return userService.getAll(index, size, isActive, isVerified, email);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GenericResponse> updateUserStatus(@PathVariable String userId, @RequestBody Admin_Req_Update_UserStatus adminUpdateUserDTO) {
        return userService.updateUserStatus(userId, adminUpdateUserDTO);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GenericResponse> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }
}
