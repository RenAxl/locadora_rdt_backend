package com.locadora_rdt_backend.modules.stocks.items.repository;

import com.locadora_rdt_backend.modules.stocks.items.model.ItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemUnitRepository extends JpaRepository<ItemUnit, Long> {

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.active = true AND unit.status = 'AVAILABLE' "
            + "AND LOWER(category.name) LIKE '%jogo%'", nativeQuery = true)
    long countAvailableGames();

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.active = true AND LOWER(category.name) LIKE '%console%'", nativeQuery = true)
    long countActiveConsoles();

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit "
            + "WHERE item_id IS NOT DISTINCT FROM :itemId "
            + "AND status IS NOT DISTINCT FROM :status AND active = true", nativeQuery = true)
    long countByItemIdAndStatusAndActiveTrue(@Param("itemId") Long itemId, @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit "
            + "WHERE item_id IS NOT DISTINCT FROM :itemId AND active = true", nativeQuery = true)
    long countByItemIdAndActiveTrue(@Param("itemId") Long itemId);

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit "
            + "WHERE item_id = :itemId AND active = true "
            + "AND status NOT IN ('AVAILABLE', 'RESERVED')", nativeQuery = true)
    long countUnavailableByItemId(@Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM tb_item_unit WHERE item_id IS NOT DISTINCT FROM :itemId "
            + "AND status IS NOT DISTINCT FROM :status AND active = true ORDER BY asset_code", nativeQuery = true)
    List<ItemUnit> findByItemIdAndStatusAndActiveTrueOrderByAssetCode(@Param("itemId") Long itemId,
                                                                   @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit "
            + "WHERE unit.item_id = :itemId AND unit.status = 'AVAILABLE' AND unit.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED'))", nativeQuery = true)
    long countAvailableForRental(@Param("itemId") Long itemId);

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "WHERE unit.item_id = :itemId AND unit.status = 'AVAILABLE' AND unit.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED')) "
            + "ORDER BY unit.asset_code", nativeQuery = true)
    List<ItemUnit> findAvailableForRental(@Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM tb_item_unit "
            + "WHERE item_id IS NOT DISTINCT FROM :itemId ORDER BY asset_code", nativeQuery = true)
    List<ItemUnit> findByItemIdOrderByAssetCode(@Param("itemId") Long itemId);

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "WHERE unit.item_id = :itemId AND unit.active = true ORDER BY unit.id FOR UPDATE OF unit",
            nativeQuery = true)
    List<ItemUnit> findActiveByItemIdForUpdate(@Param("itemId") Long itemId);

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "WHERE unit.item_id = :itemId AND unit.status = 'AVAILABLE' AND unit.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED')) "
            + "ORDER BY unit.id LIMIT :limit OFFSET :offset FOR UPDATE OF unit", nativeQuery = true)
    List<ItemUnit> findAvailableForReservation(@Param("itemId") Long itemId,
                                                @Param("limit") Integer limit,
                                                @Param("offset") long offset);

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "WHERE unit.item_id = :itemId AND unit.status = :status AND unit.active = true "
            + "AND NOT EXISTS (SELECT 1 FROM tb_rental_item_unit link "
            + "WHERE link.item_unit_id = unit.id AND link.status IN ('RESERVED', 'DELIVERED')) "
            + "ORDER BY unit.id LIMIT :limit OFFSET :offset FOR UPDATE OF unit", nativeQuery = true)
    List<ItemUnit> findByStatusForUpdate(@Param("itemId") Long itemId,
                                        @Param("status") String status,
                                        @Param("limit") Integer limit,
                                        @Param("offset") long offset);
}
