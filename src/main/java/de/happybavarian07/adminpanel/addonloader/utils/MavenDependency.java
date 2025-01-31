package de.happybavarian07.adminpanel.addonloader.utils;

/*
 * @Author HappyBavarian07
 * @Date Dezember 18, 2024 | 15:41
 */
public class MavenDependency {
    private String groupId;
    private String artifactId;
    private String version;
    private boolean isOptional = false;
    private boolean isAddonDependency = false;

    public MavenDependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, false, true);
    }

    public MavenDependency(String groupId, String artifactId, String version, boolean isOptional) {
        this(groupId, artifactId, version, isOptional, true);
    }

    public MavenDependency(String groupId, String artifactId, String version, boolean isOptional, boolean isAddonDependency) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.isOptional = isOptional;
        this.isAddonDependency = isAddonDependency;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public boolean isAddonDependency() {
        return isAddonDependency;
    }

    public void setAddonDependency(boolean addonDependency) {
        isAddonDependency = addonDependency;
    }
}
