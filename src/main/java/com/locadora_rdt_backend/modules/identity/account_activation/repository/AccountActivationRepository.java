package com.locadora_rdt_backend.modules.identity.account_activation.repository;

import com.locadora_rdt_backend.modules.identity.account_activation.model.AccountActivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AccountActivationRepository extends JpaRepository<AccountActivation, Long> {

    @Query(
            value = "SELECT * FROM tb_account_activation WHERE token = :token AND expiration > :expiration",
            nativeQuery = true
    )
    Optional<AccountActivation> findByTokenAndExpirationAfter(
            @Param("token") String token,
            @Param("expiration") Instant expiration
    );

    @Modifying
    @Query(
            value = "DELETE FROM tb_account_activation WHERE user_id = :userId",
            nativeQuery = true
    )
    void deleteByUserId(@Param("userId") Long userId);

}
