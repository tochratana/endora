package co.istad.endora.repository.postgrest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import co.istad.endora.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Fixed query to search by actual uuid field
    @Query("SELECT u FROM User u WHERE u.uuid = :uuid")
    Optional<User> findByUuid(@Param("uuid") String uuid);

    // Standard method that works with existing entity
    Optional<User> findByKeycloakUserId(String keycloakUserId);
}