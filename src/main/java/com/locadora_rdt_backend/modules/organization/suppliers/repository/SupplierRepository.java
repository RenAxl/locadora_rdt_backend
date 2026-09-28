package com.locadora_rdt_backend.modules.organization.suppliers.repository;

import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query(
            value = "SELECT * FROM tb_supplier WHERE POSITION(UPPER(:name) IN UPPER(name)) > 0",
            countQuery = "SELECT COUNT(*) FROM tb_supplier WHERE POSITION(UPPER(:name) IN UPPER(name)) > 0",
            nativeQuery = true
    )
    Page<Supplier> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_supplier "
            + "WHERE cnpj IS NOT DISTINCT FROM :cnpj AND id IS DISTINCT FROM :id)",
            nativeQuery = true)
    boolean existsByCnpjAndIdNot(@Param("cnpj") String cnpj, @Param("id") Long id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_supplier "
            + "WHERE UPPER(email) IS NOT DISTINCT FROM UPPER(:email) AND id IS DISTINCT FROM :id)",
            nativeQuery = true)
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_supplier "
            + "WHERE phone_number IS NOT DISTINCT FROM :phoneNumber AND id IS DISTINCT FROM :id)",
            nativeQuery = true)
    boolean existsByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") Long id);
}
