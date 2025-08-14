package rinsanom.com.springtwodatasoure.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import rinsanom.com.springtwodatasoure.entity.EndpointDocumentation;

import java.util.List;
import java.util.Optional;

@Repository
public interface EndpointDocumentationRepository extends MongoRepository<EndpointDocumentation, String> {
    
    Optional<EndpointDocumentation> findByTableName(String tableName);
    
    Optional<EndpointDocumentation> findByTableNameAndProjectId(String tableName, String projectId);
    
    List<EndpointDocumentation> findByProjectId(String projectId);
    
    List<EndpointDocumentation> findAllByOrderByCreatedAtDesc();
    
    void deleteByTableName(String tableName);
    
    void deleteByTableNameAndProjectId(String tableName, String projectId);
    
    boolean existsByTableName(String tableName);
    
    boolean existsByTableNameAndProjectId(String tableName, String projectId);
}
