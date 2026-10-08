package com.locadora_rdt_backend.modules.rentals.rental.repository;

import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query(
            value = "SELECT rental.* "
                    + "FROM tb_rental rental "
                    + "INNER JOIN tb_customer customer ON customer.id = rental.customer_id "
                    + "WHERE (:number = '' OR LOWER(rental.rental_number) LIKE LOWER(CONCAT('%', :number, '%'))) "
                    + "AND (:customer = '' OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :customer, '%'))) "
                    + "AND (:status = '' OR rental.status = :status) "
                    + "AND (:rentalTypeId = -1 OR rental.rental_type_id = :rentalTypeId) "
                    + "AND rental.rental_date >= :dateFrom "
                    + "AND rental.rental_date <= :dateTo",
            countQuery = "SELECT COUNT(rental.id) "
                    + "FROM tb_rental rental "
                    + "INNER JOIN tb_customer customer ON customer.id = rental.customer_id "
                    + "WHERE (:number = '' OR LOWER(rental.rental_number) LIKE LOWER(CONCAT('%', :number, '%'))) "
                    + "AND (:customer = '' OR LOWER(customer.name) LIKE LOWER(CONCAT('%', :customer, '%'))) "
                    + "AND (:status = '' OR rental.status = :status) "
                    + "AND (:rentalTypeId = -1 OR rental.rental_type_id = :rentalTypeId) "
                    + "AND rental.rental_date >= :dateFrom "
                    + "AND rental.rental_date <= :dateTo",
            nativeQuery = true
    )
    Page<Rental> find(@Param("number") String number,
                      @Param("customer") String customer,
                      @Param("status") String status,
                      @Param("rentalTypeId") Long rentalTypeId,
                      @Param("dateFrom") Instant dateFrom,
                      @Param("dateTo") Instant dateTo,
                      Pageable pageable);
}
