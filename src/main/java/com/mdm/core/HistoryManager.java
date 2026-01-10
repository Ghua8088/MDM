package com.mdm.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class HistoryManager {

    private final Path historyDir;

    public HistoryManager(Path projectRoot) {
        this.historyDir = projectRoot.resolve(".mdm/history");
    }

    public void saveSnapshot(Path pomPath, String actionDescription) throws IOException {
        if (!Files.exists(historyDir)) {
            Files.createDirectories(historyDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "pom-" + timestamp + ".xml";
        Path target = historyDir.resolve(filename);

        Files.copy(pomPath, target, StandardCopyOption.REPLACE_EXISTING);
        
        // Append to log
        Path logFile = historyDir.resolveSibling("log.txt");
        String logEntry = timestamp + " | " + filename + " | " + actionDescription + "\n";
        Files.writeString(logFile, logEntry, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }

    public void undo(Path pomPath) throws IOException {
        // Find latest snapshot
        if (!Files.exists(historyDir)) {
            throw new IllegalStateException("No history found.");
        }

        Optional<Path> lastSnapshot;
        try (Stream<Path> files = Files.list(historyDir)) {
            lastSnapshot = files
                    .filter(p -> p.toString().endsWith(".xml"))
                    .max(Comparator.comparing(Path::getFileName));
        }

        if (lastSnapshot.isPresent()) {
            Path snapshot = lastSnapshot.get();
            System.out.println("Restoring from " + snapshot.getFileName());
            Files.copy(snapshot, pomPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Should we delete the snapshot we just restored? Or keep it?
            // "Undo" implies moving back. If we delete it, we can't "redo". 
            // For now, let's just restore it.
             Files.delete(snapshot);
        } else {
             System.out.println("No snapshots available to undo.");
        }
    }
}
