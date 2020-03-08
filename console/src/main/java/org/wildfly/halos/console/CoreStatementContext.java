package org.wildfly.halos.console;

import java.util.HashMap;
import java.util.Map;

import org.wildfly.halos.console.meta.AddressTemplate;
import org.wildfly.halos.console.meta.Placeholder;
import org.wildfly.halos.console.meta.StatementContext;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.DEPLOYMENT;

public class CoreStatementContext implements StatementContext {

    private final Map<String, Placeholder> placeholders;
    private final Map<Placeholder, String> values;

    public CoreStatementContext() {
        placeholders = new HashMap<>();
        Placeholder placeholder = new Placeholder("selected.deployment", DEPLOYMENT);
        placeholders.put(placeholder.getName(), placeholder);
        values = new HashMap<>();
    }

    @Override
    public String resolve(AddressTemplate.Segment segment) {
        return null;
    }
}
