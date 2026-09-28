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

    @Query(value = "SELECT COUNT(*) FROM tb_customer WHERE active = true", nativeQuery = true)
    long countByActiveTrue();

    @Query(
            value = "SELECT * FROM tb_customer WHERE name LIKE CONCAT('%', :name, '%')",
            countQuery = "SELECT COUNT(*) FROM tb_customer WHERE name LIKE CONCAT('%', :name, '%')",
            nativeQuery = true
    )
    Page<Customer> find(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_customer WHERE cpf IS NOT DISTINCT FROM :cpf)",
            nativeQuery = true)
    boolean existsByCpf(@Param("cpf") String cpf);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_customer WHERE email IS NOT DISTINCT FROM :email)",
            nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_customer WHERE phone IS NOT DISTINCT FROM :phone)",
            nativeQuery = true)
    boolean existsByPhone(@Param("phone") String phone);

    @Query(value = "SELECT * FROM tb_customer WHERE cpf IS NOT DISTINCT FROM :cpf", nativeQuery = true)
    Customer findByCpf(@Param("cpf") String cpf);

    @Query(value = "SELECT * FROM tb_customer WHERE email IS NOT DISTINCT FROM :email", nativeQuery = true)
    Customer findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM tb_customer WHERE phone IS NOT DISTINCT FROM :phone", nativeQuery = true)
    Customer findByPhone(@Param("phone") String phone);

    @Modifying
    @Query(
            value = "DELETE FROM tb_customer WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
            value = "UPDATE tb_customer SET active = :active WHERE id = :id",
            nativeQuery = true
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active
    );

}
