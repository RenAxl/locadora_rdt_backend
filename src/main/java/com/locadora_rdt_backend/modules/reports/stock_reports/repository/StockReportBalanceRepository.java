package com.locadora_rdt_backend.modules.reports.stock_reports.repository;

import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockReportBalanceRepository extends JpaRepository<Item, Long> {

    @Query(value = "SELECT item.id AS \"itemId\", item.name AS \"itemName\", category.name AS \"categoryName\", "
            + "COUNT(unit.id) AS \"totalQuantity\", "
            + "COUNT(unit.id) FILTER (WHERE unit.status = 'AVAILABLE' AND item.active AND category.active) AS \"availableQuantity\", "
            + "COUNT(unit.id) FILTER (WHERE unit.status = 'UNAVAILABLE' OR "
            + "(unit.status = 'AVAILABLE' AND (NOT item.active OR NOT category.active))) AS \"unavailableQuantity\", "
            + "COUNT(unit.id) FILTER (WHERE unit.status = 'MAINTENANCE') AS \"maintenanceQuantity\", "
            + "COUNT(unit.id) FILTER (WHERE unit.status = 'DAMAGED') AS \"damagedQuantity\", "
            + "COUNT(unit.id) FILTER (WHERE unit.status = 'LOST') AS \"lostQuantity\", "
            + "COALESCE(balance.minimum_quantity, 0) AS \"minimumQuantity\" "
            + "FROM tb_item item JOIN tb_category category ON category.id = item.category_id "
            + "LEFT JOIN tb_stock_balance balance ON balance.item_id = item.id "
            + "LEFT JOIN tb_item_unit unit ON unit.item_id = item.id AND unit.active = true "
            + "WHERE (LOWER(item.name) LIKE LOWER('%' || :search || '%') "
            + "OR LOWER(category.name) LIKE LOWER('%' || :search || '%')) "
            + "AND (:categoryId = -1 OR item.category_id = :categoryId) "
            + "AND (:itemId = -1 OR item.id = :itemId) "
            + "AND (:active = -1 OR item.active = (:active = 1)) "
            + "GROUP BY item.id, item.name, item.active, category.id, category.name, category.active, balance.minimum_quantity "
            + "ORDER BY item.name, item.id", nativeQuery = true)
    List<StockReportBalanceRow> find(@Param("search") String search,
                                    @Param("categoryId") Long categoryId,
                                    @Param("itemId") Long itemId,
                                    @Param("active") Integer active);
}
