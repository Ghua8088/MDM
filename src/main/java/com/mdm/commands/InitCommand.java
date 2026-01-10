package com.mdm.commands;

import com.mdm.util.ConsoleUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "init", description = "Initialize a new Maven project")
public class InitCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Project Name / ArtifactId")
    private String projectName;

    @Override
    public Integer call() throws Exception {
        Path projectDir = Paths.get(projectName);
        if (Files.exists(projectDir)) {
            ConsoleUtils.error("Directory " + projectName + " already exists!");
            return 1;
        }

        ConsoleUtils.printHeader("Initializing " + projectName);

        // 1. Create Structure
        ConsoleUtils.info("Creating directory structure...");
        Files.createDirectories(projectDir.resolve("src/main/java/com/example"));
        Files.createDirectories(projectDir.resolve("src/test/java/com/example"));
        Files.createDirectories(projectDir.resolve("src/main/resources"));
        
        // 2. Generate pom.xml
        ConsoleUtils.info("Generating pom.xml...");
        String pomContent = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n\n" +
            "    <groupId>com.example</groupId>\n" +
            "    <artifactId>" + projectName + "</artifactId>\n" +
            "    <version>1.0-SNAPSHOT</version>\n\n" +
            "    <properties>\n" +
            "        <maven.compiler.source>17</maven.compiler.source>\n" +
            "        <maven.compiler.target>17</maven.compiler.target>\n" +
            "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "    </properties>\n\n" +
            "    <dependencies>\n" +
            "        <!-- Added by MDM Init -->\n" +
            "        <dependency>\n" +
            "            <groupId>org.junit.jupiter</groupId>\n" +
            "            <artifactId>junit-jupiter</artifactId>\n" +
            "            <version>5.10.0</version>\n" +
            "            <scope>test</scope>\n" +
            "        </dependency>\n" +
            "    </dependencies>\n" +
            "</project>";
        
        Files.writeString(projectDir.resolve("pom.xml"), pomContent);

        // 3. Create Main Class
        String mainClass = 
            "package com.example;\n\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello from " + projectName + "!\");\n" +
            "    }\n" +
            "}";
        Files.writeString(projectDir.resolve("src/main/java/com/example/Main.java"), mainClass);
        
        // 4. Create .gitignore (Crucial)
        String gitignore = "target/\n.idea/\n*.iml\n.vscode/\n";
        Files.writeString(projectDir.resolve(".gitignore"), gitignore);

        ConsoleUtils.success("Project " + projectName + " created successfully!");
        ConsoleUtils.info("Next steps:");
        System.out.println("  cd " + projectName);
        System.out.println("  mdm build"); // We will implement build next
        
        return 0;
    }
}
