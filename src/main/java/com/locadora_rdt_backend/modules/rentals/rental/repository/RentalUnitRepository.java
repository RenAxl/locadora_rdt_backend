package com.locadora_rdt_backend.modules.rentals.rental.repository;

import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalUnitRepository extends JpaRepository<ItemUnit, Long> {

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.item_id = :itemId AND unit.active = true AND unit.status = 'AVAILABLE' "
            + "AND item.active = true AND category.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED')) "
            + "ORDER BY unit.asset_code", nativeQuery = true)
    List<ItemUnit> findAvailableForRental(@Param("itemId") Long itemId);

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.item_id = :itemId AND unit.active = true AND unit.status = 'AVAILABLE' "
            + "AND item.active = true AND category.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED')) "
            + "ORDER BY unit.asset_code LIMIT :quantity FOR UPDATE OF unit", nativeQuery = true)
    List<ItemUnit> findAvailableForReservation(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.item_id = :itemId AND unit.active = true AND unit.status = 'AVAILABLE' "
            + "AND item.active = true AND category.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED'))", nativeQuery = true)
    long countAvailableForRental(@Param("itemId") Long itemId);

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit WHERE unit.item_id = :itemId "
            + "AND unit.active = true AND EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IS NOT DISTINCT FROM :status)", nativeQuery = true)
    long countByItemIdAndRentalStatus(@Param("itemId") Long itemId, @Param("status") String status);

    @Query(value = "SELECT * FROM tb_item_unit WHERE item_id IS NOT DISTINCT FROM :itemId ORDER BY asset_code",
            nativeQuery = true)
    List<ItemUnit> findByItemIdOrderByAssetCode(@Param("itemId") Long itemId);
}
