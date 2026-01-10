package com.mdm.commands;

import com.mdm.core.PomEditor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "remove", description = "Removes a dependency from pom.xml")
public class RemoveCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The coordinate to remove (groupId:artifactId)")
    private String coordinate;

    private final PomEditor editor;

    public RemoveCommand() {
        this.editor = new PomEditor();
    }

    @Override
    public Integer call() throws Exception {
        String[] parts = coordinate.split(":");
        if (parts.length < 2) {
            System.err.println("Invalid format. Use groupId:artifactId");
            return 2;
        }
        String groupId = parts[0];
        String artifactId = parts[1];

        Path pomPath = Paths.get("pom.xml").toAbsolutePath();
        if (!pomPath.toFile().exists()) {
             System.err.println("pom.xml not found.");
             return 1;
        }

        System.out.println("Removing " + groupId + ":" + artifactId + "...");
        boolean success = editor.removeDependency(pomPath, groupId, artifactId);

        if (success) {
            System.out.println("Dependency removed successfully.");
            return 0;
        } else {
            System.out.println("Dependency not found in pom.xml.");
            return 1;
        }
    }
}
