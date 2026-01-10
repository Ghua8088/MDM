# Title : Maven Dependency Manager CLI (MDM-CLI)

## Functional Requirements :

- **Coordinate Parsing** : The system shall parse coordinates in the  groupId:artifactId:version format and identify missing components.
- **Latest Version Fetch** : If the version is omitted, the system shall query the Maven Central API to retrieve the most recent stable version.
- **XML Injection** : The system shall programmatically insert `<dependency>` blocks into the `<dependencies>` tag of the local pom.xml.
- **Duplicate Prevention** : The system shall scan the existing pom.xml and abort the installation if the dependency already exists to prevent XML redundancy.
- **Scope Support** : The system shall support command-line flags (e.g., --test, --runtime) to set the `<scope>` tag within the dependency block.
- **Dependency Removal** : The system shall provide a remove command that locates and deletes a specific dependency block based on its GroupId and ArtifactId.
- **Dependency Listing** : The system shall display a formatted table in the console showing all current dependencies and their versions from the pom.xml.
- **Search Command** : The system shall allow users to search for libraries by keyword (e.g., `mdm search logging`) and display a list of matching coordinates from Maven Central.
- **Backup & History** : The system utilizes a `.mdm/history` folder to create snapshots of the `pom.xml` before every edit, allowing for full rollback capabilities via the `undo` command.
- **Exclusion Management** : The system shall allow users to append `<exclusions>` to a dependency during installation via optional CLI parameters.
- **Version Matching** : The system shall detect version mismatches and prompt the user to either upgrade, downgrade, or cancel the operation.

## Non Functional Requirements :

- **Strict Java Limit** : The entire system is developed using Java (LTS) without relying on external scripts.
- **Response Time** : API search queries return results under 1.5 seconds.
- **Formatting Integrity** : The system uses a text-replacement strategy combined with REGEX/Parser hybrid logic to preserve 100% of original comments and whitespace.
- **Platform Portability** : The tool executes identically on Windows, macOS, and Linux. CI/CD pipelines are provided via GitHub Actions to build native binaries for all architectures.
- **Schema Compliance** : XML modifications are schema-compliant.
- **Atomic Operations** : Failed operations do not corrupt the file.
- **Security** : No execution of remote code; text manipulation only.

## Future Scope :

1.  **Vulnerability Scanning**: Integration with OSV/CVE databases to warn users about adding vulnerable dependencies.
2.  **Multi-Module Support**: Ability to manipulate dependencies in nested Maven modules from the root directory.
3.  **Transitive Dependency Analysis**: Detecting potential classpath collisions before they happen.
4.  **Plugin Management**: Extending the CLI to manage `<build><plugins>` in addition to dependencies.
5.  **Interactive TUI**: A terminal UI mode for easier browsing of search results.

## Usage :

Build the project:
```bash
mvn package
```

Run the tool:
```bash
# General Syntax
java -jar target/mdm-cli-1.0-SNAPSHOT.jar [command] [options]

# Examples
java -jar target/mdm-cli-1.0-SNAPSHOT.jar search jackson
java -jar target/mdm-cli-1.0-SNAPSHOT.jar add junit:junit --scope test
java -jar target/mdm-cli-1.0-SNAPSHOT.jar list
java -jar target/mdm-cli-1.0-SNAPSHOT.jar remove junit:junit
java -jar target/mdm-cli-1.0-SNAPSHOT.jar undo
```
