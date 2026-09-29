package com.locadora_rdt_backend.modules.organization.employees.repository;

import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(
            value = "SELECT * FROM tb_employee WHERE name LIKE CONCAT('%', :name, '%')",
            countQuery = "SELECT COUNT(*) FROM tb_employee WHERE name LIKE CONCAT('%', :name, '%')",
            nativeQuery = true
    )
    Page<Employee> find(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_employee WHERE employee_code IS NOT DISTINCT FROM :employeeCode)",
            nativeQuery = true)
    boolean existsByEmployeeCode(@Param("employeeCode") String employeeCode);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_employee WHERE email IS NOT DISTINCT FROM :email)",
            nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_employee WHERE phone IS NOT DISTINCT FROM :phone)",
            nativeQuery = true)
    boolean existsByPhone(@Param("phone") String phone);

    @Query(value = "SELECT * FROM tb_employee WHERE employee_code IS NOT DISTINCT FROM :employeeCode", nativeQuery = true)
    Employee findByEmployeeCode(@Param("employeeCode") String employeeCode);

    @Query(value = "SELECT * FROM tb_employee WHERE email IS NOT DISTINCT FROM :email", nativeQuery = true)
    Employee findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM tb_employee WHERE phone IS NOT DISTINCT FROM :phone", nativeQuery = true)
    Employee findByPhone(@Param("phone") String phone);

    @Modifying
    @Query(
            value = "DELETE FROM tb_employee WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
            value = "UPDATE tb_employee SET active = :active WHERE id = :id",
            nativeQuery = true
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active
    );

}
