package com.locadora_rdt_backend.modules.rentals.rental_types.repository;

import com.locadora_rdt_backend.modules.rentals.rental_types.model.RentalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalTypeRepository extends JpaRepository<RentalType, Long> {

    @Query(
            value = "SELECT * FROM tb_rental_type WHERE LOWER(name) LIKE LOWER('%' || :name || '%')",
            countQuery = "SELECT COUNT(*) FROM tb_rental_type WHERE LOWER(name) LIKE LOWER('%' || :name || '%')",
            nativeQuery = true
    )
    Page<RentalType> find(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query(
            value = "DELETE FROM tb_rental_type WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
            value = "UPDATE tb_rental_type SET active = :active WHERE id = :id",
            nativeQuery = true
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active
    );

}
