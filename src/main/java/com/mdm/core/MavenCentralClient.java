package com.mdm.core;

import java.io.IOException;
import java.util.Optional;

public interface MavenCentralClient {
    /**
     * Searches for the latest stable version of a library.
     * @param groupId The user provided GroupId
     * @param artifactId The user provided ArtifactId
     * @return The latest version string if found, empty otherwise.
     * @throws IOException If network fails
     */
    Optional<String> getLatestVersion(String groupId, String artifactId) throws IOException, InterruptedException;

    /**
     * Search for artifacts matching the query.
     * @param query The user's search query
     * @return List of "g:a:v" strings
     */
    java.util.List<String> searchArtifacts(String query) throws IOException, InterruptedException;
}
