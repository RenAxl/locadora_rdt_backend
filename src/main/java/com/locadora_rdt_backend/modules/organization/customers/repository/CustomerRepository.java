package com.locadora_rdt_backend.modules.organization.customers.repository;

import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    long countByActiveTrue();

    @Query(
            value = "SELECT customer FROM Customer customer WHERE customer.name LIKE CONCAT('%', :name, '%')"
    )
    Page<Customer> find(@Param("name") String name, Pageable pageable);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Customer findByCpf(String cpf);

    Customer findByEmail(String email);

    Customer findByPhone(String phone);

    @Modifying
    @Query(
            value = "DELETE FROM Customer customer WHERE customer.id IN :ids"
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
            value = "UPDATE Customer customer SET customer.active = :active WHERE customer.id = :id"
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active
    );

}
