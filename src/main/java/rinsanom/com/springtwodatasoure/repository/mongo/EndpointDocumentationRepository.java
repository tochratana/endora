package rinsanom.com.springtwodatasoure.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import rinsanom.com.springtwodatasoure.entity.EndpointDocumentation;
import java.util.List;
import java.util.Optional;

@Repository
public interface EndpointDocumentationRepository extends MongoRepository<EndpointDocumentation, String> {
    
    Optional<EndpointDocumentation> findBySchemaName(String schemaName);

    Optional<EndpointDocumentation> findBySchemaNameAndProjectId(String schemaName, String projectId);

    List<EndpointDocumentation> findByProjectId(String projectId);
    
    List<EndpointDocumentation> findAllByOrderByCreatedAtDesc();
    
    void deleteBySchemaName(String schemaName);

    void deleteBySchemaNameAndProjectId(String schemaName, String projectId);

    boolean existsBySchemaName(String schemaName);

    boolean existsBySchemaNameAndProjectId(String schemaName, String projectId);
}
