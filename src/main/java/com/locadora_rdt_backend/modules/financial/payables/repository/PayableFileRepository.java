package com.locadora_rdt_backend.modules.financial.payables.repository;

import com.locadora_rdt_backend.modules.financial.payables.model.PayableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayableFileRepository extends JpaRepository<PayableFile, Long> {

    @Query(value = "SELECT * FROM tb_payable_file WHERE payable_id = :payableId ORDER BY id DESC",
            nativeQuery = true)
    List<PayableFile> findByPayableIdOrderByIdDesc(@Param("payableId") Long payableId);
}
