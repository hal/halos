package org.wildfly.halos.proxy;

import java.util.Objects;

/** A WildFly instance with information how to access the management endpoint */
public class Instance {

    public String name;
    public String ip;
    public int port;
    public String username;
    public String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Instance instance = (Instance) o;
        return Objects.equals(name, instance.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("%s@%s:%d", name, ip, port);
    }
}
