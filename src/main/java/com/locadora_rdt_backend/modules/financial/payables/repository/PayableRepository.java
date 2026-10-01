package com.locadora_rdt_backend.modules.financial.payables.repository;

import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface PayableRepository extends JpaRepository<Payable, Long> {

    @Query(
            value = "SELECT payable.* FROM tb_payable payable "
                    + "LEFT JOIN tb_supplier supplier ON supplier.id = payable.supplier_id "
                    + "LEFT JOIN tb_employee employee ON employee.id = payable.employee_id "
                    + "WHERE (:search IS NULL "
                    + "OR LOWER(COALESCE(payable.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(COALESCE(supplier.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(COALESCE(employee.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR CAST(payable.id AS VARCHAR) LIKE CONCAT('%', :search, '%') "
                    + "OR LOWER(COALESCE(payable.reference, '')) LIKE LOWER(CONCAT('%', :search, '%'))) "
                    + "AND (:supplierId <= 0 OR payable.supplier_id = :supplierId) "
                    + "AND (:employeeId <= 0 OR payable.employee_id = :employeeId) "
                    + "AND (:paymentMethodId <= 0 OR payable.payment_method_id = :paymentMethodId) "
                    + "AND (:paymentFrequencyId <= 0 OR payable.payment_frequency_id = :paymentFrequencyId) "
                    + "AND (:minimumAmount < 0 OR payable.amount >= :minimumAmount) "
                    + "AND (:maximumAmount < 0 OR payable.amount <= :maximumAmount) "
                    + "AND (:hasStartDate = FALSE OR (:periodType = 'DUE_DATE' AND payable.due_date >= :startDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND payable.payment_date >= :startDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(payable.created_date AS DATE) >= :startDate)) "
                    + "AND (:hasEndDate = FALSE OR (:periodType = 'DUE_DATE' AND payable.due_date <= :endDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND payable.payment_date <= :endDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(payable.created_date AS DATE) <= :endDate)) "
                    + "AND (:status = 'ALL' "
                    + "OR (:status = 'PAID' AND payable.paid = TRUE AND payable.canceled = FALSE) "
                    + "OR (:status = 'PENDING' AND payable.paid = FALSE AND payable.canceled = FALSE AND (payable.due_date IS NULL OR payable.due_date >= CURRENT_DATE)) "
                    + "OR (:status = 'OVERDUE' AND payable.paid = FALSE AND payable.canceled = FALSE AND payable.due_date < CURRENT_DATE) "
                    + "OR (:status = 'PARTIALLY_PAID' AND payable.paid = FALSE AND payable.canceled = FALSE "
                    + "AND COALESCE(payable.remaining_balance, payable.amount) > 0 "
                    + "AND COALESCE(payable.remaining_balance, payable.amount) < COALESCE(payable.amount, 0)) "
                    + "OR (:status = 'CANCELED' AND payable.canceled = TRUE)) "
                    + "ORDER BY "
                    + "CASE WHEN :orderBy = 'dueDate' AND :direction = 'ASC' THEN payable.due_date END ASC, "
                    + "CASE WHEN :orderBy = 'dueDate' AND :direction = 'DESC' THEN payable.due_date END DESC, "
                    + "CASE WHEN :orderBy = 'paymentDate' AND :direction = 'ASC' THEN payable.payment_date END ASC, "
                    + "CASE WHEN :orderBy = 'paymentDate' AND :direction = 'DESC' THEN payable.payment_date END DESC, "
                    + "CASE WHEN :orderBy = 'createdDate' AND :direction = 'ASC' THEN payable.created_date END ASC, "
                    + "CASE WHEN :orderBy = 'createdDate' AND :direction = 'DESC' THEN payable.created_date END DESC, "
                    + "CASE WHEN :orderBy = 'amount' AND :direction = 'ASC' THEN payable.amount END ASC, "
                    + "CASE WHEN :orderBy = 'amount' AND :direction = 'DESC' THEN payable.amount END DESC, "
                    + "CASE WHEN :orderBy = 'description' AND :direction = 'ASC' THEN payable.description END ASC, "
                    + "CASE WHEN :orderBy = 'description' AND :direction = 'DESC' THEN payable.description END DESC, "
                    + "payable.id DESC",
            countQuery = "SELECT COUNT(*) FROM tb_payable payable "
                    + "LEFT JOIN tb_supplier supplier ON supplier.id = payable.supplier_id "
                    + "LEFT JOIN tb_employee employee ON employee.id = payable.employee_id "
                    + "WHERE (:search IS NULL "
                    + "OR LOWER(COALESCE(payable.description, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(COALESCE(supplier.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(COALESCE(employee.name, '')) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR CAST(payable.id AS VARCHAR) LIKE CONCAT('%', :search, '%') "
                    + "OR LOWER(COALESCE(payable.reference, '')) LIKE LOWER(CONCAT('%', :search, '%'))) "
                    + "AND (:supplierId <= 0 OR payable.supplier_id = :supplierId) "
                    + "AND (:employeeId <= 0 OR payable.employee_id = :employeeId) "
                    + "AND (:paymentMethodId <= 0 OR payable.payment_method_id = :paymentMethodId) "
                    + "AND (:paymentFrequencyId <= 0 OR payable.payment_frequency_id = :paymentFrequencyId) "
                    + "AND (:minimumAmount < 0 OR payable.amount >= :minimumAmount) "
                    + "AND (:maximumAmount < 0 OR payable.amount <= :maximumAmount) "
                    + "AND (:hasStartDate = FALSE OR (:periodType = 'DUE_DATE' AND payable.due_date >= :startDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND payable.payment_date >= :startDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(payable.created_date AS DATE) >= :startDate)) "
                    + "AND (:hasEndDate = FALSE OR (:periodType = 'DUE_DATE' AND payable.due_date <= :endDate) "
                    + "OR (:periodType = 'PAYMENT_DATE' AND payable.payment_date <= :endDate) "
                    + "OR (:periodType = 'CREATED_DATE' AND CAST(payable.created_date AS DATE) <= :endDate)) "
                    + "AND (:status = 'ALL' "
                    + "OR (:status = 'PAID' AND payable.paid = TRUE AND payable.canceled = FALSE) "
                    + "OR (:status = 'PENDING' AND payable.paid = FALSE AND payable.canceled = FALSE AND (payable.due_date IS NULL OR payable.due_date >= CURRENT_DATE)) "
                    + "OR (:status = 'OVERDUE' AND payable.paid = FALSE AND payable.canceled = FALSE AND payable.due_date < CURRENT_DATE) "
                    + "OR (:status = 'PARTIALLY_PAID' AND payable.paid = FALSE AND payable.canceled = FALSE "
                    + "AND COALESCE(payable.remaining_balance, payable.amount) > 0 "
                    + "AND COALESCE(payable.remaining_balance, payable.amount) < COALESCE(payable.amount, 0)) "
                    + "OR (:status = 'CANCELED' AND payable.canceled = TRUE)) "
                    + "AND (:orderBy IS NULL OR :orderBy IS NOT NULL) "
                    + "AND (:direction IS NULL OR :direction IS NOT NULL)",
            nativeQuery = true
    )
    Page<Payable> findWithFilters(
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("hasStartDate") Boolean hasStartDate,
            @Param("hasEndDate") Boolean hasEndDate,
            @Param("status") String status,
            @Param("periodType") String periodType,
            @Param("supplierId") Long supplierId,
            @Param("employeeId") Long employeeId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("paymentFrequencyId") Long paymentFrequencyId,
            @Param("minimumAmount") BigDecimal minimumAmount,
            @Param("maximumAmount") BigDecimal maximumAmount,
            @Param("orderBy") String orderBy,
            @Param("direction") String direction,
            Pageable pageable
    );

}
