package com.bookstore.Service.Impl;

import com.bookstore.Entity.Role;
import com.bookstore.Repository.RoleRepository;
import com.bookstore.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Optional<Role> findByRoleId(String roleId) {
        return roleRepository.findByRoleId(roleId);
    }
}
