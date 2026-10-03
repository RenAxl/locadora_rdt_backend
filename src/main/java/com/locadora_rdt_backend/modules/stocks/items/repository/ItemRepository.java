package com.locadora_rdt_backend.modules.stocks.items.repository;

import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(
            value = "SELECT item.*, item.category_id AS category, item.image_data AS image, "
                    + "item.image_content_type AS imageContentType, item.created_at AS createdAt, "
                    + "item.updated_at AS updatedAt, item.created_by AS createdBy, item.updated_by AS updatedBy "
                    + "FROM tb_item item JOIN tb_category category ON category.id = item.category_id "
                    + "WHERE LOWER(item.name) LIKE LOWER('%' || :name || '%')",
            countQuery = "SELECT COUNT(*) FROM tb_item WHERE LOWER(name) LIKE LOWER('%' || :name || '%')",
            nativeQuery = true
    )
    Page<Item> find(@Param("name") String name, Pageable pageable);

    @Query(
            value = "SELECT item.*, item.category_id AS category, item.image_data AS image, "
                    + "item.image_content_type AS imageContentType, item.created_at AS createdAt, "
                    + "item.updated_at AS updatedAt, item.created_by AS createdBy, item.updated_by AS updatedBy "
                    + "FROM tb_item item JOIN tb_category category ON category.id = item.category_id "
                    + "WHERE LOWER(item.name) LIKE LOWER('%' || :name || '%') "
                    + "AND (:categoryId = -1 OR item.category_id = :categoryId)",
            countQuery = "SELECT COUNT(*) FROM tb_item WHERE LOWER(name) LIKE LOWER('%' || :name || '%') "
                    + "AND (:categoryId = -1 OR category_id = :categoryId)",
            nativeQuery = true
    )
    Page<Item> findForCatalog(@Param("name") String name,
                              @Param("categoryId") Long categoryId,
                              Pageable pageable);

    @Modifying
    @Query(
            value = "DELETE FROM tb_item WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
            value = "UPDATE tb_item SET active = :active WHERE id = :id",
            nativeQuery = true
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active
    );

}
