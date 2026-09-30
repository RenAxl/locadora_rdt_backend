package com.locadora_rdt_backend.modules.financial.payment_frequencies.repository;

import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentFrequencyRepository extends JpaRepository<PaymentFrequency, Long> {

    @Query(
            value = "SELECT *, created_at AS createdAt, updated_at AS updatedAt, "
                    + "created_by AS createdBy, updated_by AS updatedBy FROM tb_payment_frequency "
                    + "WHERE LOWER(frequency) LIKE LOWER(CONCAT('%', :frequency, '%'))",
            countQuery = "SELECT COUNT(*) FROM tb_payment_frequency WHERE LOWER(frequency) LIKE LOWER(CONCAT('%', :frequency, '%'))",
            nativeQuery = true
    )
    Page<PaymentFrequency> find(@Param("frequency") String frequency, Pageable pageable);

    @Modifying
    @Query(
            value = "DELETE FROM tb_payment_frequency WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

}
