package org.wildfly.halos.console.meta;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.DEPLOYMENT;

public class Placeholder {

    // ------------------------------------------------------ well-known placeholders

    public static final Placeholder SELECTED_RESOURCE = new Placeholder("selected.resource");
    public static final Placeholder SELECTED_DEPLOYMENT = new Placeholder("selected.deployment", DEPLOYMENT);

    // ------------------------------------------------------ instance

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
