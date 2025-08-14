package rinsanom.com.springtwodatasoure.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import rinsanom.com.springtwodatasoure.entity.TableSchema;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSchemaRepository extends MongoRepository<TableSchema, String> {
    List<TableSchema> findByProjectId(String projectId);
    Optional<TableSchema> findByTableNameAndProjectId(String tableName, String projectId);
    List<TableSchema> findAllByOrderByCreatedAtDesc();
}
