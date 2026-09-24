package com.locadora_rdt_backend.modules.organization.positions.repository;

import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query(
            value = "SELECT position FROM Position position WHERE LOWER(position.name) LIKE LOWER(CONCAT('%', :name, '%'))"
    )
    Page<Position> find(@Param("name") String name, Pageable pageable);
}
