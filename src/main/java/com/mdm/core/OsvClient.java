package com.mdm.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OsvClient {

    private final HttpClient httpClient;
    private static final String OSV_API_URL = "https://api.osv.dev/v1/query";

    public OsvClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public List<String> checkVulnerabilities(String groupId, String artifactId, String version) {
        List<String> vulnerabilities = new ArrayList<>();
        // Construct JSON payload manually to avoid deps
        // {"package": {"name": "com.google.guava:guava", "ecosystem": "Maven"}, "version": "19.0"}
        String jsonPayload = String.format(
                "{\"package\": {\"name\": \"%s:%s\", \"ecosystem\": \"Maven\"}, \"version\": \"%s\"}",
                groupId, artifactId, version
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OSV_API_URL))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                // Simple regex parsing for "id": "GHSA-..." or "summary": "..."
                // Note: deeply parsing nested JSON with regex is bad, but we just want to know IF there are vulns and maybe IDs.
                
                // If body contains "vulns", we have issues.
                if (body.contains("\"vulns\"")) {
                    Matcher m = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
                    while (m.find()) {
                        vulnerabilities.add(m.group(1));
                        if (vulnerabilities.size() >= 3) break; // Limit to 3 per lib to avoid flooding
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail for audit queries to avoid breaking flow
            System.err.println("Warning: Failed to check security for " + artifactId + ": " + e.getMessage());
        }
        return vulnerabilities;
    }
}
