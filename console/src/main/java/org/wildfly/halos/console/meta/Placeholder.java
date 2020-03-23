package org.wildfly.halos.console.meta;

public class Placeholder {

    public final String name;
    public final String resource;

    public Placeholder(String name) {
        this(name, null);
    }

    public Placeholder(String name, String resource) {
        this.name = name;
        this.resource = resource;
    }

    /** @return the {@code name} surrounded by "{" and "}" */
    public String expression() {
        return "{" + name + "}";
    }
}
