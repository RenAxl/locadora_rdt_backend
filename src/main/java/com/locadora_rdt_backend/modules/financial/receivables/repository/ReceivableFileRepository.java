package com.locadora_rdt_backend.modules.financial.receivables.repository;

import com.locadora_rdt_backend.modules.financial.receivables.model.ReceivableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceivableFileRepository extends JpaRepository<ReceivableFile, Long> {

    @Query(value = "SELECT * FROM tb_receivable_file WHERE receivable_id = :receivableId ORDER BY id DESC",
            nativeQuery = true)
    List<ReceivableFile> findByReceivableIdOrderByIdDesc(@Param("receivableId") Long receivableId);
}
