package com.bookstore.Repository;

import com.bookstore.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(String name);

    Optional<Role> findByRoleId(String roleId);
}
