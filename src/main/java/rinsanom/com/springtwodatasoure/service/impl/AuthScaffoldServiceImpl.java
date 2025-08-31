package rinsanom.com.springtwodatasoure.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rinsanom.com.springtwodatasoure.entity.EndpointDocumentation;
import rinsanom.com.springtwodatasoure.repository.mongo.EndpointDocumentationRepository;
import rinsanom.com.springtwodatasoure.service.AuthScaffoldService;

import java.util.Map;
import java.util.Optional;

/**
 * Default implementation that stores minimal documentation for
 * generated authentication endpoints.  It does not create runtime
 * endpoints – those are handled by {@code ProjectAuthController} –
 * but this service provides discoverability in the existing
 * dynamic documentation store.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthScaffoldServiceImpl implements AuthScaffoldService {

    private final EndpointDocumentationRepository endpointDocumentationRepository;

    @Override
    public void generateDefaultAuthEndpoints(String projectId, Map<String, String> userTableSchema) {
        String basePath = "/client-api/" + projectId + "/auth"; // Updated path

        StringBuilder doc = new StringBuilder();
        doc.append("Authentication Endpoints\n");
        doc.append("=======================\n");
        doc.append("POST ").append(basePath).append("/register - Register a new user\n");
        doc.append("POST ").append(basePath).append("/login - Login and receive JWT token\n");
        doc.append("GET ").append(basePath).append("/me - Get current user (requires Authorization header)\n");
        doc.append("\nUser API Endpoints\n");
        doc.append("==================\n");
        doc.append("GET /client-api/").append(projectId).append("/{tableName} - Get all records\n");
        doc.append("POST /client-api/").append(projectId).append("/{tableName} - Create record\n");
        doc.append("GET /client-api/").append(projectId).append("/{tableName}/{id} - Get record by ID\n");
        doc.append("PUT /client-api/").append(projectId).append("/{tableName}/{id} - Update record\n");
        doc.append("DELETE /client-api/").append(projectId).append("/{tableName}/{id} - Delete record\n");

        Optional<EndpointDocumentation> existing = endpointDocumentationRepository
                .findBySchemaNameAndProjectId("auth", projectId);

        EndpointDocumentation endpointDoc = existing
                .orElseGet(() -> new EndpointDocumentation("auth", projectId, doc.toString()));

        endpointDoc.setRawDocumentation(doc.toString());
        endpointDoc.setBasePath(basePath);
        endpointDoc.setUpdatedAt();

        endpointDocumentationRepository.save(endpointDoc);
        log.info("Generated auth endpoints for project {}", projectId);
    }
}
