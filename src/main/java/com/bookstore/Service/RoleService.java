package com.bookstore.Service;

import com.bookstore.Entity.Role;

import java.util.Optional;

public interface RoleService {
    Role findByName(String name);

    Optional<Role> findByRoleId(String roleId);
}
