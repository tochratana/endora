package rinsanom.com.springtwodatasoure.repository.postgrest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rinsanom.com.springtwodatasoure.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Custom query if you want to map UUID to a different field
    @Query("SELECT u FROM User u WHERE u.keycloakUserId = :uuid")
    Optional<User> findByUuid(@Param("uuid") String uuid);

    // Standard method that works with existing entity
    Optional<User> findByKeycloakUserId(String keycloakUserId);
}