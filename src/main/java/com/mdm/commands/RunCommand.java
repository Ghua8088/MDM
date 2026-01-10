package com.mdm.commands;

import com.mdm.util.ConsoleUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "run", description = "Run the project Main class")
public class RunCommand implements Callable<Integer> {

    @Option(names = {"-m", "--main"}, description = "Main class to run (default: com.example.Main)")
    private String mainClass = "com.example.Main";

    @Override
    public Integer call() throws Exception {
        ConsoleUtils.printHeader("Running " + mainClass);
        
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String mvn = isWindows ? "mvn.cmd" : "mvn";
        if (java.nio.file.Files.exists(java.nio.file.Paths.get("mvnw"))) {
            mvn = isWindows ? ".\\mvnw.cmd" : "./mvnw";
        }

        String cmd = mvn + " -q exec:java -Dexec.mainClass=\"" + mainClass + "\"";
        ConsoleUtils.info("Executing: " + cmd);

        ProcessBuilder pb = new ProcessBuilder(isWindows ? "cmd.exe" : "bash", isWindows ? "/c" : "-c", cmd);
        pb.inheritIO();
        Process p = pb.start();
        return p.waitFor();
    }
}
