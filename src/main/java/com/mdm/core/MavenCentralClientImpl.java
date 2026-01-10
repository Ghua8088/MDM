package com.mdm.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Simple implementation using standard Java HTTP Client
public class MavenCentralClientImpl implements MavenCentralClient {

    private final HttpClient httpClient;
    private static final String SEARCH_URL = "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&rows=1&wt=json";

    public MavenCentralClientImpl() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Optional<String> getLatestVersion(String groupId, String artifactId) throws IOException, InterruptedException {
        String queryUrl = String.format(SEARCH_URL, groupId, artifactId);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queryUrl))
                .timeout(Duration.ofSeconds(3)) // Fast failure requirement
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseVersionFromJson(response.body());
        }
        return Optional.empty();
    }

    // Quick and dirty regex JSON parsing to avoid heavy dependencies just for one field
    // Response looks like: "latestVersion":"1.2.3"
    private Optional<String> parseVersionFromJson(String json) {
        Pattern pattern = Pattern.compile("\"latestVersion\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    @Override
    public java.util.List<String> searchArtifacts(String query) throws IOException, InterruptedException {
        // Search query: q=query&rows=10
        String url = "https://search.maven.org/solrsearch/select?q=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8) + "&rows=10&wt=json";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        java.util.List<String> results = new java.util.ArrayList<>();
        if (response.statusCode() == 200) {
            // Parse JSON response. Response structure: { "response": { "docs": [ { "g": "...", "a": "...", "latestVersion": "..." } ] } }
            // Using regex to find all docs objects. 
            // NOTE: This regex is brittle. Ideally use Jackson, but for low deps constraint, we iterate.
            
            String json = response.body();
            // Find "docs":[ ... ] block
            int docsStart = json.indexOf("\"docs\":[");
            if (docsStart != -1) {
                 // Roughly split by "id": to get each object
                 String[] items = json.substring(docsStart).split("\"id\":");
                 for (String item : items) {
                     // Extract g, a, latestVersion
                     String g = extractField(item, "g");
                     String a = extractField(item, "a");
                     String v = extractField(item, "latestVersion");
                     
                     if (g != null && a != null && v != null) {
                         results.add(g + ":" + a + ":" + v);
                     }
                 }
            }
        }
        return results;
    }
    
    private String extractField(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }
}
