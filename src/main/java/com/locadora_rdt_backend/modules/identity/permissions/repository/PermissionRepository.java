package com.locadora_rdt_backend.modules.identity.permissions.repository;

import com.locadora_rdt_backend.modules.identity.permissions.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByGroupNameIgnoreCaseOrderByNameAsc(String groupName);

    List<Permission> findAllByOrderByGroupNameAscNameAsc();

    @Query("SELECT DISTINCT p.groupName FROM Permission p ORDER BY p.groupName")
    List<String> findDistinctGroupNames();
}
