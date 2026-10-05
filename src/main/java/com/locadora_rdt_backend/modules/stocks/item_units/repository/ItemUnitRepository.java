package com.locadora_rdt_backend.modules.stocks.item_units.repository;

import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemUnitRepository extends JpaRepository<ItemUnit, Long> {

    @Query(
            value = "SELECT unit.*, unit.asset_code AS assetCode, unit.serial_number AS serialNumber, "
                    + "unit.condition_status AS conditionStatus, unit.purchase_date AS purchaseDate, "
                    + "unit.created_at AS createdAt, unit.updated_at AS updatedAt, "
                    + "unit.created_by AS createdBy, unit.updated_by AS updatedBy "
                    + "FROM tb_item_unit unit JOIN tb_item item ON item.id = unit.item_id "
                    + "WHERE (LOWER(unit.asset_code) LIKE LOWER('%' || :name || '%') "
                    + "OR LOWER(item.name) LIKE LOWER('%' || :name || '%')) "
                    + "AND (:itemId = -1 OR unit.item_id = :itemId)",
            countQuery = "SELECT COUNT(*) FROM tb_item_unit unit JOIN tb_item item ON item.id = unit.item_id "
                    + "WHERE (LOWER(unit.asset_code) LIKE LOWER('%' || :name || '%') "
                    + "OR LOWER(item.name) LIKE LOWER('%' || :name || '%')) "
                    + "AND (:itemId = -1 OR unit.item_id = :itemId)",
            nativeQuery = true
    )
    Page<ItemUnit> find(@Param("name") String name, @Param("itemId") Long itemId, Pageable pageable);

    @Query(value = "SELECT * FROM tb_item_unit WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<ItemUnit> findByIdForUpdate(@Param("id") Long id);

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

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "WHERE unit.item_id = :itemId AND unit.active = true ORDER BY unit.id FOR UPDATE OF unit",
            nativeQuery = true)
    List<ItemUnit> findActiveByItemIdForUpdate(@Param("itemId") Long itemId);

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
