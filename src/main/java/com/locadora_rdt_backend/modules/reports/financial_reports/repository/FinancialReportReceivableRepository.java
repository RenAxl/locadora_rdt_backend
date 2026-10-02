package com.locadora_rdt_backend.modules.reports.financial_reports.repository;

import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialReportReceivableRepository extends JpaRepository<Receivable, Long> {

    @Query(
            value = "SELECT r.* FROM tb_receivable r "
                    + "LEFT JOIN tb_customer c ON c.id = r.customer_id "
                    + "WHERE (CAST(:search AS TEXT) IS NULL "
                    + "OR LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) "
                    + "OR LOWER(COALESCE(r.reference, '')) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) "
                    + "OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))) "
                    + "AND (:customerId <= 0 OR r.customer_id = :customerId) "
                    + "AND (:paymentMethodId <= 0 OR r.payment_method_id = :paymentMethodId) "
                    + "AND (:minimumAmount < 0 OR r.amount >= :minimumAmount) "
                    + "AND (:maximumAmount < 0 OR r.amount <= :maximumAmount) "
                    + "AND (:hasStartDate = FALSE OR (:periodType = 'DUE_DATE' AND r.due_date >= :startDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND r.payment_date >= :startDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(r.created_date AS DATE) >= :startDate)) "
                    + "AND (:hasEndDate = FALSE OR (:periodType = 'DUE_DATE' AND r.due_date <= :endDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND r.payment_date <= :endDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(r.created_date AS DATE) <= :endDate)) "
                    + "AND (:status = 'ALL' "
                    + "OR (:status = 'PAID' AND r.paid = TRUE AND r.canceled = FALSE) "
                    + "OR (:status = 'PENDING' AND r.paid = FALSE AND r.canceled = FALSE AND (r.due_date IS NULL OR r.due_date >= CURRENT_DATE)) "
                    + "OR (:status = 'OVERDUE' AND r.paid = FALSE AND r.canceled = FALSE AND r.due_date < CURRENT_DATE) "
                    + "OR (:status = 'PARTIALLY_PAID' AND r.paid = FALSE AND r.canceled = FALSE "
                    + "AND COALESCE(r.remaining_balance, r.amount) > 0 "
                    + "AND COALESCE(r.remaining_balance, r.amount) < COALESCE(r.amount, 0)) "
                    + "OR (:status = 'CANCELED' AND r.canceled = TRUE)) "
                    + "ORDER BY r.id DESC",
            nativeQuery = true
    )
    List<Receivable> find(
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("hasStartDate") Boolean hasStartDate,
            @Param("hasEndDate") Boolean hasEndDate,
            @Param("status") String status,
            @Param("periodType") String periodType,
            @Param("customerId") Long customerId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("minimumAmount") BigDecimal minimumAmount,
            @Param("maximumAmount") BigDecimal maximumAmount
    );
}
