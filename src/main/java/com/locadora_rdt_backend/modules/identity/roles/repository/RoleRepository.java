package com.locadora_rdt_backend.modules.identity.roles.repository;

import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE LOWER(r.authority) LIKE LOWER(CONCAT('%', :authority, '%'))")
    Page<Role> findByAuthorityLikeIgnoreCase(@Param("authority") String authority, Pageable pageable);

    @Query(
            "SELECT r.id, COUNT(p) " +
                    "FROM Role r " +
                    "LEFT JOIN r.permissions p " +
                    "WHERE r.id IN :roleIds " +
                    "GROUP BY r.id"
    )
    List<Object[]> countPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);




}
