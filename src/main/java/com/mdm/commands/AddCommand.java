package com.mdm.commands;

import com.mdm.core.MavenCentralClient;
import com.mdm.core.MavenCentralClientImpl;
import com.mdm.core.PomEditor;
import com.mdm.model.Dependency;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "add", description = "Adds a dependency to the local pom.xml")
public class AddCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The coordinate (groupId:artifactId:[version])")
    private String coordinate;

    @Option(names = {"-s", "--scope"}, description = "Dependency scope (compile, test, provided, etc.)")
    private String scope;
    
    // We can inject these for testing, but default to real impls
    private final MavenCentralClient client;
    private final PomEditor editor;

    public AddCommand() {
        this.client = new MavenCentralClientImpl();
        this.editor = new PomEditor();
    }

    // Constructor for testing
    public AddCommand(MavenCentralClient client, PomEditor editor) {
        this.client = client;
        this.editor = editor;
    }

    @Override
    public Integer call() throws Exception {
        String[] parts = coordinate.split(":");
        if (parts.length < 2 || parts.length > 3) {
            System.err.println("Invalid coordinate format. Use groupId:artifactId or groupId:artifactId:version");
            return 2; // Invalid Input
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = (parts.length == 3) ? parts[2] : null;

        if (version == null) {
            System.out.println("No version specified. Fetching latest version for " + groupId + ":" + artifactId + "...");
            version = client.getLatestVersion(groupId, artifactId)
                    .orElseThrow(() -> new RuntimeException("Could not find version for " + groupId + ":" + artifactId));
            System.out.println("Resolved version: " + version);
        }

        Dependency dep = new Dependency(groupId, artifactId, version);
        if (scope != null) {
            dep.setScope(scope);
        }

        System.out.println("Adding dependency: " + dep);
        
        // Assume pom.xml is in current directory for now
        Path pomPath = Paths.get("pom.xml").toAbsolutePath();
        if (!pomPath.toFile().exists()) {
             System.err.println("pom.xml not found in current directory: " + pomPath);
             return 1;
        }

        editor.addDependency(pomPath, dep);
        System.out.println("Success! Dependency added to pom.xml");

        return 0;
    }
}
