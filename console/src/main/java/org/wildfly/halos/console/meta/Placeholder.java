package org.wildfly.halos.console.meta;

public class Placeholder {

    private final String name;
    private final String resource;

    public Placeholder(String name, String resource) {
        this.name = name;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    /** @return the {@code name} surrounded by "{" and "}" */
    public String getExpression() {
        return "{" + name + "}";
    }

    public String getResource() {
        return resource;
    }
}
