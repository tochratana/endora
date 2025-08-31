package co.istad.endora.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import co.istad.endora.entity.TableSchema;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSchemaRepository extends MongoRepository<TableSchema, String> {
    List<TableSchema> findByProjectId(String projectId);
    Optional<TableSchema> findBySchemaNameAndProjectId(String schemaName, String projectId);
    List<TableSchema> findAllByOrderByCreatedAtDesc();
}
