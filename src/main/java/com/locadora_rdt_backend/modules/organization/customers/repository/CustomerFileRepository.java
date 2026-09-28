package com.locadora_rdt_backend.modules.organization.customers.repository;

import com.locadora_rdt_backend.modules.organization.customers.model.CustomerFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerFileRepository extends JpaRepository<CustomerFile, Long> {

    @Query(value = "SELECT * FROM tb_customer_file WHERE customer_id = :customerId ORDER BY id DESC",
            nativeQuery = true)
    List<CustomerFile> findByCustomerIdOrderByIdDesc(@Param("customerId") Long customerId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_customer_file "
            + "WHERE customer_id = :customerId AND UPPER(name) = UPPER(:name))",
            nativeQuery = true)
    boolean existsByCustomerIdAndNameIgnoreCase(@Param("customerId") Long customerId,
                                               @Param("name") String name);
}
