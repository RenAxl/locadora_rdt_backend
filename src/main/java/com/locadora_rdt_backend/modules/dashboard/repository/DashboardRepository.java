package com.locadora_rdt_backend.modules.dashboard.repository;

import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface DashboardRepository extends JpaRepository<Rental, Long> {

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.active = true AND unit.status = 'AVAILABLE' "
            + "AND LOWER(category.name) LIKE '%jogo%'", nativeQuery = true)
    long countAvailableGames();

    @Query(value = "SELECT COUNT(*) FROM tb_item_unit unit "
            + "JOIN tb_item item ON item.id = unit.item_id "
            + "JOIN tb_category category ON category.id = item.category_id "
            + "WHERE unit.active = true AND LOWER(category.name) LIKE '%console%'", nativeQuery = true)
    long countActiveConsoles();

    @Query(value = "SELECT COUNT(*) FROM tb_rental WHERE status IS NOT DISTINCT FROM :status",
            nativeQuery = true)
    long countByStatus(@Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM tb_rental "
            + "WHERE status IS NOT DISTINCT FROM :status AND actual_return_date BETWEEN :start AND :end",
            nativeQuery = true)
    long countByStatusAndEffectiveReturnDateBetween(
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query(value = "SELECT COUNT(*) FROM tb_customer WHERE active = true", nativeQuery = true)
    long countByActiveTrue();

    @Query(value = "SELECT COUNT(*) FROM tb_rental "
            + "WHERE status IS NOT DISTINCT FROM :status AND expected_return_date < :date",
            nativeQuery = true)
    long countByStatusAndReturnForecastDateBefore(@Param("status") String status,
                                                @Param("date") Instant date);

    @Query(value = "SELECT COUNT(*) FROM tb_rental WHERE rental_date BETWEEN :start AND :end",
            nativeQuery = true)
    long countByRegistrationDateBetween(@Param("start") Instant start, @Param("end") Instant end);
}
