package com.locadora_rdt_backend.modules.rentals.rental.repository;

import com.locadora_rdt_backend.modules.rentals.rental.model.RentalStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalStatusHistoryRepository extends JpaRepository<RentalStatusHistory, Long> {

    @Query(value = "SELECT * FROM tb_rental_status_history "
            + "WHERE rental_id IS NOT DISTINCT FROM :rentalId ORDER BY changed_at ASC", nativeQuery = true)
    List<RentalStatusHistory> findByRentalIdOrderByChangedAtAsc(@Param("rentalId") Long rentalId);

    @Modifying
    @Query(value = "DELETE FROM tb_rental_status_history WHERE rental_id IS NOT DISTINCT FROM :rentalId",
            nativeQuery = true)
    void deleteByRentalId(@Param("rentalId") Long rentalId);

}
