package com.locadora_rdt_backend.modules.rentals.rental.repository;

import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalItemUnitRepository extends JpaRepository<RentalItemUnit, Long> {

    @Query(value = "SELECT unit.* FROM tb_rental_item_unit unit "
            + "JOIN tb_rental_item item ON item.id = unit.rental_item_id "
            + "WHERE item.rental_id IS NOT DISTINCT FROM :rentalId ORDER BY unit.id", nativeQuery = true)
    List<RentalItemUnit> findByRentalItemRentalIdOrderById(@Param("rentalId") Long rentalId);

    @Query(value = "SELECT COUNT(*) FROM tb_rental_item_unit "
            + "WHERE rental_item_id IS NOT DISTINCT FROM :rentalItemId AND status IN (:statuses)", nativeQuery = true)
    long countByRentalItemIdAndStatusIn(@Param("rentalItemId") Long rentalItemId,
                                       @Param("statuses") List<String> statuses);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tb_rental_item_unit "
            + "WHERE item_unit_id IS NOT DISTINCT FROM :itemUnitId AND status IN (:statuses))", nativeQuery = true)
    boolean existsByItemUnitIdAndStatusIn(@Param("itemUnitId") Long itemUnitId,
                                         @Param("statuses") List<String> statuses);

    @Modifying
    @Query(value = "DELETE FROM tb_rental_item_unit WHERE rental_item_id IN "
            + "(SELECT id FROM tb_rental_item WHERE rental_id IS NOT DISTINCT FROM :rentalId)", nativeQuery = true)
    void deleteByRentalItemRentalId(@Param("rentalId") Long rentalId);

}
