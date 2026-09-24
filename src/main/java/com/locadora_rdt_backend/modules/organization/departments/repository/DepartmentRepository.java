package com.locadora_rdt_backend.modules.organization.departments.repository;

import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query(
            value = "SELECT department FROM Department department WHERE department.name LIKE CONCAT('%', :name, '%')"
    )
    Page<Department> find(@Param("name") String name, Pageable pageable);
}
