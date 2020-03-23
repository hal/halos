package org.wildfly.halos.console.core;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.wildfly.halos.console.meta.AddressTemplate;
import org.wildfly.halos.console.meta.Placeholder;
import org.wildfly.halos.console.meta.ResolveException;
import org.wildfly.halos.console.meta.StatementContext;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.DEPLOYMENT;

@Singleton
public class CoreStatementContext implements StatementContext {

    // ------------------------------------------------------ well-known placeholders

    public static final Placeholder SELECTED_RESOURCE = new Placeholder("selected.resource");
    public static final Placeholder SELECTED_DEPLOYMENT = new Placeholder("selected.deployment", DEPLOYMENT);

    // ------------------------------------------------------ instance

    private final Map<String, Placeholder> placeholders;
    private final Map<Placeholder, String> values;

    public CoreStatementContext() {
        placeholders = new HashMap<>();
        values = new HashMap<>();
        add(SELECTED_RESOURCE);
        add(SELECTED_DEPLOYMENT);
    }

    @Override
    public AddressTemplate.Segment resolve(AddressTemplate.Segment segment) {
        if (segment.containsPlaceholder()) {
            String name = segment.placeholder();
            Placeholder placeholder = placeholder(name);
            if (placeholder != null) {
                String resolvedValue = value(placeholder);
                if (resolvedValue != null) {
                    if (segment.hasKey()) {
                        // key={placeholder}
                        return new AddressTemplate.Segment(segment.key, resolvedValue);
                    } else {
                        // {placeholder}
                        return new AddressTemplate.Segment(placeholder.resource, resolvedValue);
                    }
                } else {
                    throw new ResolveException(
                            "No value found for placeholder " + placeholder.name + " in segment " + segment);
                }
            } else {
                throw new ResolveException("Unknown placeholder " + name + " in segment " + segment);
            }
        } else {
            return segment;
        }
    }

    @Override
    public void add(Placeholder placeholder) {
        placeholders.put(placeholder.name, placeholder);
    }

    @Override
    public Placeholder placeholder(String name) {
        return placeholders.get(name);
    }

    @Override
    public void assign(String placeholder, String value) {
        Placeholder p = placeholders.get(placeholder);
        if (p != null) {
            values.put(p, value);
        }
    }

    @Override
    public String value(Placeholder placeHolder) {
        return values.get(placeHolder);
    }
}
