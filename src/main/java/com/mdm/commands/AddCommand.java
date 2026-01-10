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

        // Check if exists using a quick read (using PomEditor internal or ad-hoc check? 
        // Better to try add, if PomEditor says exists, we update. 
        // But PomEditor.addDependency is void and handles text.
        // Let's peek at file content for existence check here? Or modify PomEditor.addDependency to return status?
        // Modifying AddCommand to use editor.updateVersion if add fails/checks positive.
        
        // We will move the check logic here or rely on editor.
        // Let's try to update first? No, logic: Add if missing, Update if present.
        
        // Simple check:
        // We know checking raw text is flaky, but for CLI it's fast.
        // Better: We'll modify the AddCommand flow to check via list/search? 
        // Let's just trust our editor's helper if we made it public, or use the one we just added.
        // Wait, PomEditor.hasDependency is private. I'll make it redundant by trying update first?
        // No, `updateVersion` returns false if not found.
        
        System.out.println("Processing dependency: " + dep);
        
        // Assume pom.xml is in current directory for now
        Path pomPath = Paths.get("pom.xml").toAbsolutePath();
        if (!pomPath.toFile().exists()) {
             System.err.println("pom.xml not found in current directory: " + pomPath);
             return 1;
        }

        // Strategy: Try Update. If false (not found), then Add.
        if (editor.updateVersion(pomPath, groupId, artifactId, version)) {
             System.out.println("Dependency exists! Updated version to " + version);
        } else {
             editor.addDependency(pomPath, dep);
             System.out.println("Dependency added to pom.xml");
        }

        return 0;
    }
}
