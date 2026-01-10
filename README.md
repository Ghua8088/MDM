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
- **Backup Creation** : Before modifying the pom.xml, the system shall create a temporary backup (e.g., pom.xml.bak) to ensure data can be recovered in case of a crash.
- **Exclusion Management** : The system shall allow users to append `<exclusions>` to a dependency during installation via optional CLI parameters.
- **Version Matching** : The system shall detect version mismatches and prompt the user to either upgrade, downgrade, or cancel the operation.

## Non Functional Requirements :

- **Strict Java Limitation** : The entire system must be developed using Java 21 or higher (LTS) without relying on external scripts (Python, Bash, etc.).
- **Response Time** : API search queries shall return results and update the UI in under 1.5 seconds under normal 2026 network conditions.
- **Formatting Integrity** : The system must preserve the original indentation style (tabs vs. spaces) and comments of the pom.xml during the write process.
- **Platform Portability** : The tool must execute with identical behavior on Windows 11, macOS Sequoia, and Linux (Ubuntu 24.04+).
- **Resource Footprint** : The CLI tool must consume less than 128MB of RAM during peak operation (XML parsing and network fetching).
- **Error Transparency** : The system shall provide meaningful exit codes (e.g., 0 for success, 1 for network error, 2 for invalid XML) for CI/CD pipeline integration.
- **Schema Compliance** : Any XML modified by the tool must pass validation against the official Maven XSD v4.0.0.
- **Atomic Operations** : The system must ensure that a failed write operation does not leave the pom.xml in a corrupted or "half-written" state.
- **Offline Graceful Failure** : If no internet connection is detected, the system must immediately inform the user rather than hanging or timing out indefinitely.
- **Security (No-Execute)** : The tool shall only modify text files; it must not execute any downloaded JARs or scripts from the internet to ensure developer environment safety.

## Software Requirements :

- Language: Java 21+
- XML Library: javax.xml.parsers (DOM) or FasterXML Jackson-dataformat-xml.
- CLI Library: Picocli (Highly recommended for modern Java CLIs).
- HTTP Client: java.net.http.HttpClient (Native Java 11+).
