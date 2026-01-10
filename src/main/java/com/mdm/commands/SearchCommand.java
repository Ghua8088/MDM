package com.mdm.commands;

import com.mdm.core.MavenCentralClient;
import com.mdm.core.MavenCentralClientImpl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "search", description = "Search for artifacts on Maven Central")
public class SearchCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Query keyword")
    private String query;

    private final MavenCentralClient client;

    public SearchCommand() {
        this.client = new MavenCentralClientImpl();
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Searching for '" + query + "'...");
        try {
            List<String> results = client.searchArtifacts(query);
            
            if (results.isEmpty()) {
                System.out.println("No results found.");
            } else {
                System.out.printf("%-30s | %-30s | %-15s%n", "Group ID", "Artifact ID", "Latest Ver");
                System.out.println("--------------------------------------------------------------------------------");
                for (String coord : results) {
                    String[] parts = coord.split(":");
                    System.out.printf("%-30s | %-30s | %-15s%n", parts[0], parts[1], parts[2]);
                }
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            return 1;
        }
    }
}
