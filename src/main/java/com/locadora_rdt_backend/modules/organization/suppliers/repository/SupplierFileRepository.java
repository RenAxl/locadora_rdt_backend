package com.locadora_rdt_backend.modules.organization.suppliers.repository;

import com.locadora_rdt_backend.modules.organization.suppliers.model.SupplierFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierFileRepository extends JpaRepository<SupplierFile, Long> {

    @Query(value = "SELECT * FROM tb_supplier_file WHERE supplier_id = :supplierId ORDER BY id DESC",
            nativeQuery = true)
    List<SupplierFile> findBySupplierIdOrderByIdDesc(@Param("supplierId") Long supplierId);
}
