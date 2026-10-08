package com.locadora_rdt_backend.modules.rentals.rental.repository;

import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {

    @Query(value = "SELECT * FROM tb_rental_item WHERE rental_id IS NOT DISTINCT FROM :rentalId ORDER BY id",
            nativeQuery = true)
    List<RentalItem> findByRentalIdOrderById(@Param("rentalId") Long rentalId);

    @Modifying
    @Query(value = "DELETE FROM tb_rental_item WHERE rental_id IS NOT DISTINCT FROM :rentalId", nativeQuery = true)
    void deleteByRentalId(@Param("rentalId") Long rentalId);

}
