package com.tiendaweb.admin.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaweb.common.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

}
