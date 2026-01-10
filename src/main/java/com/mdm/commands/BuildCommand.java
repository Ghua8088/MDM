package com.mdm.commands;

import com.mdm.util.ConsoleUtils;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

@Command(name = "build", description = "Compiles and packages the project (Wrapper for mvn package)")
public class BuildCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        ConsoleUtils.printHeader("Building Project");
        
        // Detect OS for command
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        // Prefer ./mvnw if exists
        String cmd = isWindows ? "mvn.cmd" : "mvn";
        if (java.nio.file.Files.exists(java.nio.file.Paths.get("mvnw"))) {
            cmd = isWindows ? ".\\mvnw.cmd" : "./mvnw";
            ConsoleUtils.info("Using Maven Wrapper...");
        }

        ConsoleUtils.info("Running: " + cmd + " package");
        
        ProcessBuilder pb = new ProcessBuilder(isWindows ? "cmd.exe" : "bash", isWindows ? "/c" : "-c", cmd + " package");
        pb.inheritIO(); // Stream output directly to console
        Process p = pb.start();
        int exitCode = p.waitFor();

        if (exitCode == 0) {
            ConsoleUtils.success("Build Successful! JAR is in target/");
        } else {
            ConsoleUtils.error("Build Failed. See output above.");
        }
        return exitCode;
    }
}
