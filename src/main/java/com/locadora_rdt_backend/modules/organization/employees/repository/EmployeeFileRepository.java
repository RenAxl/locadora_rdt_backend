package com.locadora_rdt_backend.modules.organization.employees.repository;

import com.locadora_rdt_backend.modules.organization.employees.model.EmployeeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeFileRepository extends JpaRepository<EmployeeFile, Long> {

    @Query(value = "SELECT * FROM tb_employee_file WHERE employee_id = :employeeId ORDER BY id DESC",
            nativeQuery = true)
    List<EmployeeFile> findByEmployeeIdOrderByIdDesc(@Param("employeeId") Long employeeId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_employee_file "
            + "WHERE employee_id = :employeeId AND UPPER(name) = UPPER(:name))",
            nativeQuery = true)
    boolean existsByEmployeeIdAndNameIgnoreCase(@Param("employeeId") Long employeeId,
                                               @Param("name") String name);
}
