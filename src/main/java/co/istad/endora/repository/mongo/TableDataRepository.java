package co.istad.endora.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import co.istad.endora.entity.TableData;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableDataRepository extends MongoRepository<TableData, String> {
    List<TableData> findBySchemaNameAndProjectId(String schemaName, String projectId);
    List<TableData> findBySchemaName(String schemaName);
    List<TableData> findByProjectId(String projectId);
    Optional<TableData> findByIdAndSchemaNameAndProjectId(String id, String schemaName, String projectId);
    void deleteBySchemaNameAndProjectId(String schemaName, String projectId);
    void deleteByIdAndSchemaNameAndProjectId(String id, String schemaName, String projectId);
}
