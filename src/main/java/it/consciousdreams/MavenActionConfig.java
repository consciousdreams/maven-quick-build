package it.consciousdreams;

import java.util.Objects;

public class MavenActionConfig {
    public String id;
    public String label;
    public String goals;    // e.g. "clean install -Dmaven.test.skip=true"
    public String iconPath = "/icons/maven_install.svg";

    public MavenActionConfig() {}

    public MavenActionConfig(String id, String label, String goals, String iconPath) {
        this.id = id;
        this.label = label;
        this.goals = goals;
        this.iconPath = iconPath;
    }

    public MavenActionConfig copy() {
        return new MavenActionConfig(id, label, goals, iconPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MavenActionConfig)) return false;
        MavenActionConfig that = (MavenActionConfig) o;
        return Objects.equals(id, that.id)
                && Objects.equals(label, that.label)
                && Objects.equals(goals, that.goals)
                && Objects.equals(iconPath, that.iconPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, goals, iconPath);
    }
}
