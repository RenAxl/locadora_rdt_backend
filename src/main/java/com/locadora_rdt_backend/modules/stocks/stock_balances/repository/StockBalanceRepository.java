package com.locadora_rdt_backend.modules.stocks.stock_balances.repository;

import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {

    @Query(
            value = "SELECT balance.* FROM tb_stock_balance balance "
                    + "JOIN tb_item item ON item.id = balance.item_id "
                    + "LEFT JOIN tb_category category ON category.id = item.category_id "
                    + "WHERE LOWER(item.name) LIKE LOWER('%' || :name || '%')",
            countQuery = "SELECT COUNT(*) FROM tb_stock_balance balance "
                    + "JOIN tb_item item ON item.id = balance.item_id "
                    + "WHERE LOWER(item.name) LIKE LOWER('%' || :name || '%')",
            nativeQuery = true
    )
    Page<StockBalance> find(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * FROM tb_stock_balance WHERE item_id IS NOT DISTINCT FROM :itemId",
            nativeQuery = true)
    Optional<StockBalance> findByItemId(@Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM tb_stock_balance WHERE item_id = :itemId FOR UPDATE", nativeQuery = true)
    Optional<StockBalance> findByItemIdForUpdate(@Param("itemId") Long itemId);

    @Query(value = "SELECT balance.* FROM tb_stock_balance balance "
            + "JOIN tb_item_unit unit ON unit.item_id = balance.item_id "
            + "WHERE unit.id = :unitId FOR UPDATE OF balance", nativeQuery = true)
    Optional<StockBalance> findByItemUnitIdForUpdate(@Param("unitId") Long unitId);

    @Query(value = "SELECT * FROM tb_stock_balance WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<StockBalance> findByIdForUpdate(@Param("id") Long id);
}
