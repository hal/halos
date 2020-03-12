package org.wildfly.halos.console.config;

import java.util.Objects;

/** Class holding information about a WildFly instance. */
public class Instance {

    public final String name;
    public String organization;
    public String productName;
    public String productVersion;
    public String releaseName;
    public String releaseVersion;
    public User user;
    public Roles roles;
    public Version managementVersion;
    public AccessControlProvider accessControlProvider;

    public Instance(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Instance instance = (Instance) o;
        return name.equals(instance.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Instance(" + name + ')';
    }
}
