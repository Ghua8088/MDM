package com.mdm.core;

import com.mdm.model.Dependency;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles manipulation of pom.xml content with a focus on preserving formatting.
 * Uses text-based analysis rather than DOM parsing to ensure comments and whitespace remain untouched.
 */
public class PomEditor {

    // Regex to find dependencies block: <dependencies> ... </dependencies>
    // We capture the closing tag to insert before it.
    private static final Pattern DEPENDENCIES_TAG_PATTERN = Pattern.compile("(<dependencies>)(.*?)(</dependencies>)", Pattern.DOTALL);
    
    // Regex to check for existing dependency (simplified)
    // Matches: <groupId>...</groupId> followed eventually by <artifactId>...</artifactId> inside a dependency block
    // This is a heuristic; a full parser is safer for checking duplicates, but we do text match for speed and preserving format.
    
    public void addDependency(Path pomPath, Dependency dependency) throws IOException {
        // 1. Create History Snapshot
        // Assuming project root is parent of pom.xml
        HistoryManager history = new HistoryManager(pomPath.getParent());
        history.saveSnapshot(pomPath, "Added " + dependency);

        // 2. Read Content
        String content = Files.readString(pomPath, StandardCharsets.UTF_8);

        // 3. Check for duplicates
        if (hasDependency(content, dependency)) {
            System.out.println("Dependency " + dependency + " already exists in pom.xml. Skipping.");
            return;
        }

        // 4. Find insertion point
        String newContent = insertDependency(content, dependency);

        // 5. Write back
        Files.writeString(pomPath, newContent, StandardCharsets.UTF_8);
    }

    private boolean hasDependency(String content, Dependency dep) {
        // Simple string search for the groupId and artifactId to avoid complex parsing overkill for now.
        // A robust implementation would parse the <dependencies> block.
        // Logic: specific groupId followed nearby by specific artifactId
        return content.contains("<groupId>" + dep.getGroupId() + "</groupId>") &&
               content.contains("<artifactId>" + dep.getArtifactId() + "</artifactId>");
    }

    public boolean removeDependency(Path pomPath, String groupId, String artifactId) throws IOException {
        HistoryManager history = new HistoryManager(pomPath.getParent());
        history.saveSnapshot(pomPath, "Removed " + groupId + ":" + artifactId);
        
        String content = Files.readString(pomPath, StandardCharsets.UTF_8);
        
        // Strategy: Find <dependency> blocks, check if they contain our G:A, and remove the whole block.
        // We use a pattern that captures the whole dependency tag content.
        Pattern depPattern = Pattern.compile("(\\s*<dependency>)(.*?)(</dependency>)", Pattern.DOTALL);
        Matcher matcher = depPattern.matcher(content);
        
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        boolean found = false;
        
        while (matcher.find()) {
            String fullBlock = matcher.group(0);     // precise content including tags
            String innerContent = matcher.group(2);  // content inside tags
            
            if (innerContent.contains("<groupId>" + groupId + "</groupId>") && 
                innerContent.contains("<artifactId>" + artifactId + "</artifactId>")) {
                // Found it! Skip this block (append everything before it)
                sb.append(content, lastEnd, matcher.start());
                lastEnd = matcher.end();
                found = true;
                // If there's a following newline/empty line, we might want to consume it to leave no gap.
                // But simplified: just removing the block is usually enough.
            }
        }
        sb.append(content.substring(lastEnd)); // Append rest of file
        
        if (found) {
            Files.writeString(pomPath, sb.toString(), StandardCharsets.UTF_8);
            
            // Clean up history if we want (actually we keep history for Undo)
            return true;
        }
        return false;
    }
    
    private String insertDependency(String content, Dependency dep) {
        Matcher matcher = DEPENDENCIES_TAG_PATTERN.matcher(content);
        if (matcher.find()) {
            // Found <dependencies> block
            String dependenciesContent = matcher.group(2);
            String closingTag = matcher.group(3); // </dependencies>
            
            // Detect indentation from the line containing </dependencies>
            // We look at the line ending with </dependencies>
            int closingIndex = matcher.start(3);
            String indent = detectIndentation(content, closingIndex);

            String dependencyXml = dep.toXml(indent + "    "); // recursive indent
            
            // We insert before the closing tag, ensuring we have a newline
            StringBuilder sb = new StringBuilder();
            sb.append(content, 0, closingIndex);
            
            // If the block was empty or on one line, we might need newlines
            if (!dependenciesContent.trim().isEmpty() && !dependenciesContent.endsWith("\n")) {
               sb.append("\n");
            }
            
            sb.append(dependencyXml).append("\n");
            sb.append(indent).append(closingTag);
            sb.append(content.substring(matcher.end(3)));
            
            return sb.toString();
        } else {
            // No <dependencies> tag found. Need to insert it into <project>.
            // Find </project>
            int projectCloseIndex = content.lastIndexOf("</project>");
            if (projectCloseIndex == -1) {
                throw new IllegalStateException("Invalid pom.xml: No </project> tag found.");
            }
            
            String indent = detectIndentation(content, projectCloseIndex);
            
            StringBuilder sb = new StringBuilder();
            sb.append(content, 0, projectCloseIndex);
            sb.append(indent).append("<dependencies>\n");
            sb.append(dep.toXml(indent + "    ")).append("\n");
            sb.append(indent).append("</dependencies>\n\n");
            sb.append(content.substring(projectCloseIndex)); // </project>...
            return sb.toString();
        }
    }

    private String detectIndentation(String content, int tagIndex) {
        // Walk backwards from tagIndex to find the start of the line
        int i = tagIndex - 1;
        while (i >= 0 && content.charAt(i) != '\n' && content.charAt(i) != '\r') {
            i--;
        }
        // Capture whitespace from start of line to tagIndex
        // i currently points to newline char. i+1 is start of line.
        String lineStart = content.substring(i + 1, tagIndex);
        if (lineStart.isBlank()) {
            return lineStart; // This is the indentation
        }
        return "    "; // Default 4 spaces if mixed
    }
}
