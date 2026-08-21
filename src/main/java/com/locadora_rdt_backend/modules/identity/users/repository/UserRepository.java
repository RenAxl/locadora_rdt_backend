package com.locadora_rdt_backend.modules.identity.users.repository;

import com.locadora_rdt_backend.modules.identity.users.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(
            value = "SELECT * FROM tb_user WHERE name ILIKE CONCAT('%', :name, '%')",
            countQuery = "SELECT COUNT(*) FROM tb_user WHERE name ILIKE CONCAT('%', :name, '%')",
            nativeQuery = true
    )
    Page<User> find(@Param("name") String name, Pageable pageable);

    @Query(
            value = "SELECT * FROM tb_user WHERE email = :email",
            nativeQuery = true
    )
    User findByEmail(@Param("email") String email);

    @Query(
            value = "SELECT * FROM tb_user WHERE telephone = :telephone",
            nativeQuery = true
    )
    User findByTelephone(@Param("telephone") String telephone);

    @Modifying
    @Query(
            value = "DELETE FROM tb_user WHERE id IN (:ids)",
            nativeQuery = true
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(
            value = "UPDATE tb_user SET active = :active WHERE id = :id",
            nativeQuery = true
    )
    int updateActiveById(
            @Param("id") Long id,
            @Param("active") boolean active
    );
}