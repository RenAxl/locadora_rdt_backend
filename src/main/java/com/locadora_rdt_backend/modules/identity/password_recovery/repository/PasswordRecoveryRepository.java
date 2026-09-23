package com.locadora_rdt_backend.modules.identity.password_recovery.repository;

import com.locadora_rdt_backend.modules.identity.password_recovery.model.PasswordRecoveryToken;
import com.locadora_rdt_backend.modules.identity.password_recovery.model.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecoveryToken, Long> {

    @Query(
            value = "SELECT token FROM PasswordRecoveryToken token "
                    + "WHERE token.token = :token AND token.type = :type AND token.expiration > :now"
    )
    Optional<PasswordRecoveryToken> findByTokenAndTypeAndExpirationAfter(
            @Param("token") String token,
            @Param("type") TokenType type,
            @Param("now") Instant now
    );

    void deleteByUserId(Long userId);

    void deleteByUserIdAndType(Long userId, TokenType type);
}
