package com.locadora_rdt_backend.modules.stocks.stock_movements.repository;

import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query(
            value = "SELECT movement.*, movement.created_at AS createdAt, "
                    + "movement.created_by AS createdBy "
                    + "FROM tb_stock_movement movement JOIN tb_item item ON item.id = movement.item_id "
                    + "WHERE LOWER(item.name) LIKE LOWER('%' || CAST(:name AS text) || '%')",
            countQuery = "SELECT COUNT(*) FROM tb_stock_movement movement "
                    + "JOIN tb_item item ON item.id = movement.item_id "
                    + "WHERE LOWER(item.name) LIKE LOWER('%' || CAST(:name AS text) || '%')",
            nativeQuery = true
    )
    Page<StockMovement> find(@Param("name") String name, Pageable pageable);
}
