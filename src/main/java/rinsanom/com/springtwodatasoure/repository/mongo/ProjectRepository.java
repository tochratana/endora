package rinsanom.com.springtwodatasoure.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import rinsanom.com.springtwodatasoure.entity.Projects;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Projects, String> {
    Optional<Projects> findByProjectUuid(String projectUuid);
    List<Projects> findByUserUuid(String userUuid); // Added method to find projects by user UUID
}
