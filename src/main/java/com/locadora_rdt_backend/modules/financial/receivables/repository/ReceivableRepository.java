package com.locadora_rdt_backend.modules.financial.receivables.repository;

import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ReceivableRepository extends JpaRepository<Receivable, Long> {

    @Query(
            value = "SELECT receivable.* FROM tb_receivable receivable "
                    + "LEFT JOIN tb_customer customer ON customer.id = receivable.customer_id "
                    + "WHERE (:search IS NULL "
                    + "OR LOWER(COALESCE(receivable.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(COALESCE(customer.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR CAST(receivable.id AS VARCHAR) LIKE CONCAT('%', :search, '%') "
                    + "OR LOWER(COALESCE(receivable.reference, '')) LIKE LOWER(CONCAT('%', :search, '%'))) "
                    + "AND (:customerId <= 0 OR receivable.customer_id = :customerId) "
                    + "AND (:paymentMethodId <= 0 OR receivable.payment_method_id = :paymentMethodId) "
                    + "AND (:paymentFrequencyId <= 0 OR receivable.payment_frequency_id = :paymentFrequencyId) "
                    + "AND (:minimumAmount < 0 OR receivable.amount >= :minimumAmount) "
                    + "AND (:maximumAmount < 0 OR receivable.amount <= :maximumAmount) "
                    + "AND (:hasStartDate = FALSE OR (:periodType = 'DUE_DATE' AND receivable.due_date >= :startDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND receivable.payment_date >= :startDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(receivable.created_date AS DATE) >= :startDate)) "
                    + "AND (:hasEndDate = FALSE OR (:periodType = 'DUE_DATE' AND receivable.due_date <= :endDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND receivable.payment_date <= :endDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(receivable.created_date AS DATE) <= :endDate)) "
                    + "AND (:status = 'ALL' "
                    + "OR (:status = 'PAID' AND receivable.paid = TRUE AND receivable.canceled = FALSE) "
                    + "OR (:status = 'PENDING' AND receivable.paid = FALSE AND receivable.canceled = FALSE AND (receivable.due_date IS NULL OR receivable.due_date >= CURRENT_DATE)) "
                    + "OR (:status = 'OVERDUE' AND receivable.paid = FALSE AND receivable.canceled = FALSE AND receivable.due_date < CURRENT_DATE) "
                    + "OR (:status = 'PARTIALLY_PAID' AND receivable.paid = FALSE AND receivable.canceled = FALSE "
                    + "AND COALESCE(receivable.remaining_balance, receivable.amount) > 0 "
                    + "AND COALESCE(receivable.remaining_balance, receivable.amount) < COALESCE(receivable.amount, 0)) "
                    + "OR (:status = 'CANCELED' AND receivable.canceled = TRUE)) "
                    + "ORDER BY "
                    + "CASE WHEN :orderBy = 'dueDate' AND :direction = 'ASC' THEN receivable.due_date END ASC, "
                    + "CASE WHEN :orderBy = 'dueDate' AND :direction = 'DESC' THEN receivable.due_date END DESC, "
                    + "CASE WHEN :orderBy = 'paymentDate' AND :direction = 'ASC' THEN receivable.payment_date END ASC, "
                    + "CASE WHEN :orderBy = 'paymentDate' AND :direction = 'DESC' THEN receivable.payment_date END DESC, "
                    + "CASE WHEN :orderBy = 'createdDate' AND :direction = 'ASC' THEN receivable.created_date END ASC, "
                    + "CASE WHEN :orderBy = 'createdDate' AND :direction = 'DESC' THEN receivable.created_date END DESC, "
                    + "CASE WHEN :orderBy = 'amount' AND :direction = 'ASC' THEN receivable.amount END ASC, "
                    + "CASE WHEN :orderBy = 'amount' AND :direction = 'DESC' THEN receivable.amount END DESC, "
                    + "CASE WHEN :orderBy = 'description' AND :direction = 'ASC' THEN receivable.description END ASC, "
                    + "CASE WHEN :orderBy = 'description' AND :direction = 'DESC' THEN receivable.description END DESC, "
                    + "receivable.id DESC",
            countQuery = "SELECT COUNT(*) FROM tb_receivable receivable "
                    + "LEFT JOIN tb_customer customer ON customer.id = receivable.customer_id "
                    + "WHERE (:search IS NULL "
                    + "OR LOWER(COALESCE(receivable.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(COALESCE(customer.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR CAST(receivable.id AS VARCHAR) LIKE CONCAT('%', :search, '%') "
                    + "OR LOWER(COALESCE(receivable.reference, '')) LIKE LOWER(CONCAT('%', :search, '%'))) "
                    + "AND (:customerId <= 0 OR receivable.customer_id = :customerId) "
                    + "AND (:paymentMethodId <= 0 OR receivable.payment_method_id = :paymentMethodId) "
                    + "AND (:paymentFrequencyId <= 0 OR receivable.payment_frequency_id = :paymentFrequencyId) "
                    + "AND (:minimumAmount < 0 OR receivable.amount >= :minimumAmount) "
                    + "AND (:maximumAmount < 0 OR receivable.amount <= :maximumAmount) "
                    + "AND (:hasStartDate = FALSE OR (:periodType = 'DUE_DATE' AND receivable.due_date >= :startDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND receivable.payment_date >= :startDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(receivable.created_date AS DATE) >= :startDate)) "
                    + "AND (:hasEndDate = FALSE OR (:periodType = 'DUE_DATE' AND receivable.due_date <= :endDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND receivable.payment_date <= :endDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(receivable.created_date AS DATE) <= :endDate)) "
                    + "AND (:status = 'ALL' "
                    + "OR (:status = 'PAID' AND receivable.paid = TRUE AND receivable.canceled = FALSE) "
                    + "OR (:status = 'PENDING' AND receivable.paid = FALSE AND receivable.canceled = FALSE AND (receivable.due_date IS NULL OR receivable.due_date >= CURRENT_DATE)) "
                    + "OR (:status = 'OVERDUE' AND receivable.paid = FALSE AND receivable.canceled = FALSE AND receivable.due_date < CURRENT_DATE) "
                    + "OR (:status = 'PARTIALLY_PAID' AND receivable.paid = FALSE AND receivable.canceled = FALSE "
                    + "AND COALESCE(receivable.remaining_balance, receivable.amount) > 0 "
                    + "AND COALESCE(receivable.remaining_balance, receivable.amount) < COALESCE(receivable.amount, 0)) "
                    + "OR (:status = 'CANCELED' AND receivable.canceled = TRUE)) "
                    + "AND (:orderBy IS NULL OR :orderBy IS NOT NULL) "
                    + "AND (:direction IS NULL OR :direction IS NOT NULL)",
            nativeQuery = true
    )
    Page<Receivable> findWithFilters(
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("hasStartDate") Boolean hasStartDate,
            @Param("hasEndDate") Boolean hasEndDate,
            @Param("status") String status,
            @Param("periodType") String periodType,
            @Param("customerId") Long customerId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("paymentFrequencyId") Long paymentFrequencyId,
            @Param("minimumAmount") BigDecimal minimumAmount,
            @Param("maximumAmount") BigDecimal maximumAmount,
            @Param("orderBy") String orderBy,
            @Param("direction") String direction,
            Pageable pageable
    );

}
