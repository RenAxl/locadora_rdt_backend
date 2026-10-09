package com.locadora_rdt_backend.modules.reports.rental_reports.repository;

import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface RentalReportRepository extends JpaRepository<Rental, Long> {

    @Query(
            value = "SELECT r.* FROM tb_rental r "
                    + "LEFT JOIN tb_customer c ON c.id = r.customer_id "
                    + "LEFT JOIN tb_rental_type t ON t.id = r.rental_type_id "
                    + "WHERE (CAST(:search AS TEXT) IS NULL "
                    + "OR LOWER(COALESCE(r.rental_number, '')) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) "
                    + "OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) "
                    + "OR LOWER(COALESCE(t.name, '')) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))) "
                    + "AND (:customerId <= 0 OR r.customer_id = :customerId) "
                    + "AND (:rentalTypeId <= 0 OR r.rental_type_id = :rentalTypeId) "
                    + "AND (:paymentMethodId <= 0 OR r.payment_method_id = :paymentMethodId) "
                    + "AND (:status = 'ALL' OR r.status = :status) "
                    + "AND (:minimumAmount < 0 OR COALESCE(r.total_amount, 0) + COALESCE(r.late_fee, 0) "
                    + "- COALESCE(r.discount, 0) >= :minimumAmount) "
                    + "AND (:maximumAmount < 0 OR COALESCE(r.total_amount, 0) + COALESCE(r.late_fee, 0) "
                    + "- COALESCE(r.discount, 0) <= :maximumAmount) "
                    + "AND (:hasStartDate = FALSE OR (CASE :periodType "
                    + "WHEN 'REGISTRATION_DATE' THEN r.rental_date WHEN 'RENTAL_START_DATE' THEN r.start_date "
                    + "WHEN 'RETURN_FORECAST_DATE' THEN r.expected_return_date "
                    + "WHEN 'EFFECTIVE_RETURN_DATE' THEN r.actual_return_date END) >= :startDate) "
                    + "AND (:hasEndDate = FALSE OR (CASE :periodType "
                    + "WHEN 'REGISTRATION_DATE' THEN r.rental_date WHEN 'RENTAL_START_DATE' THEN r.start_date "
                    + "WHEN 'RETURN_FORECAST_DATE' THEN r.expected_return_date "
                    + "WHEN 'EFFECTIVE_RETURN_DATE' THEN r.actual_return_date END) < :endDate) "
                    + "ORDER BY r.id DESC",
            nativeQuery = true
    )
    List<Rental> find(
            @Param("search") String search,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("hasStartDate") Boolean hasStartDate,
            @Param("hasEndDate") Boolean hasEndDate,
            @Param("status") String status,
            @Param("periodType") String periodType,
            @Param("customerId") Long customerId,
            @Param("rentalTypeId") Long rentalTypeId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("minimumAmount") BigDecimal minimumAmount,
            @Param("maximumAmount") BigDecimal maximumAmount
    );
}
