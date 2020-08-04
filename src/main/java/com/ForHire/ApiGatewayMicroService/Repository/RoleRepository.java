package com.ForHire.ApiGatewayMicroService.Repository;


import com.ForHire.ApiGatewayMicroService.Entity.ENUM_Roles;
import com.ForHire.ApiGatewayMicroService.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(ENUM_Roles name);

}
