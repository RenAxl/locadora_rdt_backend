package com.locadora_rdt_backend.modules.financial.payment_methods.repository;

import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // Aliases preservam a ordenação pelos nomes dos campos de auditoria.
    @Query(
            value = "SELECT payment_method.*, payment_method.created_at AS createdAt, "
                    + "payment_method.updated_at AS updatedAt, payment_method.created_by AS createdBy, "
                    + "payment_method.updated_by AS updatedBy FROM tb_payment_method payment_method "
                    + "WHERE LOWER(payment_method.name) LIKE LOWER('%' || :name || '%')",
            countQuery = "SELECT COUNT(*) FROM tb_payment_method "
                    + "WHERE LOWER(name) LIKE LOWER('%' || :name || '%')",
            nativeQuery = true
    )
    Page<PaymentMethod> find(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query(
            value = "DELETE FROM tb_payment_method WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
