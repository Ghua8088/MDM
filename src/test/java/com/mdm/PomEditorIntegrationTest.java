package com.mdm;

import com.mdm.core.PomEditor;
import com.mdm.model.Dependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PomEditorIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void testAddDependencyToExistingBlock() throws IOException {
        // ARRANGE
        Path pom = tempDir.resolve("pom.xml");
        String originalContent = """
                <project>
                    <!-- Some comment that must survive -->
                    <dependencies>
                        <dependency>
                            <groupId>old</groupId>
                            <artifactId>lib</artifactId>
                        </dependency>
                    </dependencies>
                </project>""";
        Files.writeString(pom, originalContent);

        PomEditor editor = new PomEditor();
        Dependency newDep = new Dependency("com.test", "new-lib", "1.0.0");

        // ACT
        editor.addDependency(pom, newDep);

        // ASSERT
        String newContent = Files.readString(pom);
        System.out.println("Modified POM:\n" + newContent);

        Assertions.assertTrue(newContent.contains("<!-- Some comment that must survive -->"), "Comment should be preserved");
        Assertions.assertTrue(newContent.contains("<groupId>com.test</groupId>"), "New dependency should be present");
        Assertions.assertTrue(newContent.contains("<groupId>old</groupId>"), "Old dependency should be preserved");
        
        // Check history existence (New architecture)
        Path historyDir = pom.getParent().resolve(".mdm/history");
        Assertions.assertTrue(Files.exists(historyDir), "History directory should exist");
        
        try (java.util.stream.Stream<Path> files = Files.list(historyDir)) {
             long snapshotCount = files.filter(p -> p.toString().endsWith(".xml")).count();
             Assertions.assertTrue(snapshotCount > 0, "At least one history snapshot should exist in " + historyDir);
        }
    }
    
     @Test
    void testAddDependencyToNoDependenciesBlock() throws IOException {
        // ARRANGE
        Path pom = tempDir.resolve("pom.xml");
        String originalContent = """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                </project>""";
        Files.writeString(pom, originalContent);

        PomEditor editor = new PomEditor();
        Dependency newDep = new Dependency("com.test", "solo-lib", "2.0");

        // ACT
        editor.addDependency(pom, newDep);

        // ASSERT
        String newContent = Files.readString(pom);
        
        Assertions.assertTrue(newContent.contains("<dependencies>"), "Dependencies block should be created");
        Assertions.assertTrue(newContent.contains("<artifactId>solo-lib</artifactId>"), "Artifact should be present");
    }
}
