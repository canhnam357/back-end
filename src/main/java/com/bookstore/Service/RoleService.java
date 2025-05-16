package com.bookstore.Service;

import com.bookstore.Entity.Role;

import java.util.Optional;

public interface RoleService {
    Optional<Role> findByName(String name);
}
