package com.locadora_rdt_backend.modules.stocks.categories.repository;

import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(
            value = "SELECT * FROM tb_category WHERE LOWER(name) LIKE LOWER('%' || :name || '%')",
            countQuery = "SELECT COUNT(*) FROM tb_category WHERE LOWER(name) LIKE LOWER('%' || :name || '%')",
            nativeQuery = true
    )
    Page<Category> find(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query(
            value = "DELETE FROM tb_category WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = "UPDATE tb_category SET active = :active, updated_at = CURRENT_TIMESTAMP, "
                    + "updated_by = :updatedBy, version = version + 1 WHERE id = :id",
            nativeQuery = true
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active,
            @Param("updatedBy") String updatedBy
    );

}
