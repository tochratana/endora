package rinsanom.com.springtwodatasoure.repository.postgrest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rinsanom.com.springtwodatasoure.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
//    Optional<User> findByEmail(String email);
//    boolean existsByEmail(String email);
//    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByUuid(String uuid); // Added method to find by UUID
    Optional<User> findByKeycloakUserId(String keycloakUserId);
}
