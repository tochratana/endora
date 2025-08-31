package co.istad.endora.repository.postgrest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import co.istad.endora.entity.PasswordResetTokenEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByResetTokenAndUsedFalseAndExpiresAtAfter(
            String resetToken, LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetTokenEntity p SET p.used = true WHERE p.email = :email AND p.used = false")
    int markUsedByEmail(@Param("email") String email);
}
