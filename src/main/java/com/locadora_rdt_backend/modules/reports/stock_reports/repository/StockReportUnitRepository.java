package com.locadora_rdt_backend.modules.reports.stock_reports.repository;

import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockReportUnitRepository extends JpaRepository<ItemUnit, Long> {

    @Query(value = "SELECT unit.* FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE (LOWER(item.name) LIKE LOWER('%' || :search || '%') "
            + "OR LOWER(category.name) LIKE LOWER('%' || :search || '%') "
            + "OR LOWER(unit.asset_code) LIKE LOWER('%' || :search || '%')) "
            + "AND (:categoryId = -1 OR item.category_id = :categoryId) "
            + "AND (:itemId = -1 OR item.id = :itemId) "
            + "AND (:active = -1 OR unit.active = (:active = 1)) "
            + "AND (:status = 'ALL' OR (unit.active = true AND "
            + "CASE WHEN unit.status = 'AVAILABLE' AND (NOT item.active OR NOT category.active) "
            + "THEN 'UNAVAILABLE' ELSE unit.status END = :status)) "
            + "AND (:conditionStatus = 'ALL' OR unit.condition_status = :conditionStatus) "
            + "ORDER BY item.name, unit.asset_code", nativeQuery = true)
    List<ItemUnit> find(@Param("search") String search,
                        @Param("categoryId") Long categoryId,
                        @Param("itemId") Long itemId,
                        @Param("active") Integer active,
                        @Param("status") String status,
                        @Param("conditionStatus") String conditionStatus);
}
