package rinsanom.com.springtwodatasoure.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import rinsanom.com.springtwodatasoure.entity.Projects;

public interface ProjectRepository extends MongoRepository<Projects, String> {

}
