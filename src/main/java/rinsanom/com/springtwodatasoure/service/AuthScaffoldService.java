package rinsanom.com.springtwodatasoure.service;

import java.util.Map;

/**
 * Service used to generate default authentication endpoints
 * when a project defines a user table.  Implementations can
 * create controller documentation or other artefacts required
 * for the auth flow.
 */
public interface AuthScaffoldService {

    /**
     * Generates the basic authentication endpoint metadata for a project.
     *
     * @param projectId the project that owns the user table
     * @param userTableSchema schema definition of the created user table
     */
    void generateDefaultAuthEndpoints(String projectId, Map<String, String> userTableSchema);
}
