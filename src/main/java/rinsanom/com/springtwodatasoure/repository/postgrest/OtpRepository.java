package rinsanom.com.springtwodatasoure.repository.postgrest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rinsanom.com.springtwodatasoure.entity.OtpEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findByEmailAndOtpCodeAndPurposeAndUsedFalseAndExpiresAtAfter(
            String email, String otpCode, String purpose, LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpEntity o WHERE o.expiresAt < :now")
    int deleteExpiredOtps(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE OtpEntity o SET o.used = true WHERE o.email = :email AND o.purpose = :purpose AND o.used = false")
    int markUsedByEmailAndPurpose(@Param("email") String email, @Param("purpose") String purpose);

    Optional<OtpEntity> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, String purpose);
}