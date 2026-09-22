package com.locadora_rdt_backend.modules.organization.customers.repository;

import com.locadora_rdt_backend.modules.organization.customers.model.CustomerFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerFileRepository extends JpaRepository<CustomerFile, Long> {

    List<CustomerFile> findByCustomerIdOrderByIdDesc(Long customerId);

    boolean existsByCustomerIdAndNameIgnoreCase(Long customerId, String name);
}
