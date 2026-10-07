package com.locadora_rdt_backend.modules.reports.stock_reports.repository;

import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface StockReportMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query(value = "SELECT movement.* FROM tb_stock_movement movement "
            + "JOIN tb_item item ON item.id = movement.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "LEFT JOIN tb_item_unit unit ON unit.id = movement.item_unit_id "
            + "WHERE (LOWER(item.name) LIKE LOWER('%' || :search || '%') "
            + "OR LOWER(category.name) LIKE LOWER('%' || :search || '%') "
            + "OR LOWER(COALESCE(unit.asset_code, '')) LIKE LOWER('%' || :search || '%') "
            + "OR LOWER(COALESCE(movement.reason, '')) LIKE LOWER('%' || :search || '%')) "
            + "AND (:categoryId = -1 OR item.category_id = :categoryId) "
            + "AND (:itemId = -1 OR item.id = :itemId) "
            + "AND (:movementType = 'ALL' OR movement.type = :movementType) "
            + "AND (:hasStartDate = false OR movement.created_at >= :startDate) "
            + "AND (:hasEndDate = false OR movement.created_at < :endDateExclusive) "
            + "ORDER BY movement.created_at DESC, movement.id DESC", nativeQuery = true)
    List<StockMovement> find(@Param("search") String search,
                             @Param("categoryId") Long categoryId,
                             @Param("itemId") Long itemId,
                             @Param("movementType") String movementType,
                             @Param("startDate") Instant startDate,
                             @Param("endDateExclusive") Instant endDateExclusive,
                             @Param("hasStartDate") boolean hasStartDate,
                             @Param("hasEndDate") boolean hasEndDate);
}
