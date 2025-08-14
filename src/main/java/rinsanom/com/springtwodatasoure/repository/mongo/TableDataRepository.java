package rinsanom.com.springtwodatasoure.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import rinsanom.com.springtwodatasoure.entity.TableData;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableDataRepository extends MongoRepository<TableData, String> {
    List<TableData> findByTableNameAndProjectId(String tableName, String projectId);
    List<TableData> findByTableName(String tableName);
    List<TableData> findByProjectId(String projectId);
    Optional<TableData> findByIdAndTableNameAndProjectId(String id, String tableName, String projectId);
    void deleteByTableNameAndProjectId(String tableName, String projectId);
    void deleteByIdAndTableNameAndProjectId(String id, String tableName, String projectId);
}
