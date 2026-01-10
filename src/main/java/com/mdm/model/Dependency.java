package com.mdm.model;

import java.util.Objects;
import java.util.Optional;

public class Dependency {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;

    public Dependency() {}

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + (version != null ? version : "LATEST");
    }

    public String toXml(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("<dependency>\n");
        sb.append(indent).append(indent).append("<groupId>").append(groupId).append("</groupId>\n");
        sb.append(indent).append(indent).append("<artifactId>").append(artifactId).append("</artifactId>\n");
        if (version != null && !version.isEmpty()) {
            sb.append(indent).append(indent).append("<version>").append(version).append("</version>\n");
        }
        if (scope != null && !scope.isEmpty()) {
            sb.append(indent).append(indent).append("<scope>").append(scope).append("</scope>\n");
        }
        sb.append(indent).append("</dependency>");
        return sb.toString();
    }
    
    // Simple equality check for duplicate detection (usually G:A is enough, but exact match might check V too)
    public boolean matches(String g, String a) {
        return Objects.equals(this.groupId, g) && Objects.equals(this.artifactId, a);
    }
}
