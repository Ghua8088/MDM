package com.mdm.commands;

import com.mdm.core.HistoryManager;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "undo", description = "Reverts the last change to pom.xml")
public class UndoCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        Path pomPath = Paths.get("pom.xml").toAbsolutePath();
        HistoryManager history = new HistoryManager(pomPath.getParent());

        try {
            history.undo(pomPath);
            System.out.println("Undo successful! pom.xml restored to previous state.");
            return 0;
        } catch (Exception e) {
            System.err.println("Undo failed: " + e.getMessage());
            return 1;
        }
    }
}
