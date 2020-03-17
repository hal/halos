package org.wildfly.halos.console;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.wildfly.halos.console.meta.AddressTemplate;
import org.wildfly.halos.console.meta.Placeholder;
import org.wildfly.halos.console.meta.ResolveException;
import org.wildfly.halos.console.meta.StatementContext;

import static org.wildfly.halos.console.meta.Placeholder.SELECTED_DEPLOYMENT;
import static org.wildfly.halos.console.meta.Placeholder.SELECTED_RESOURCE;

@Singleton
public class CoreStatementContext implements StatementContext {

    private final Map<String, Placeholder> placeholders;
    private final Map<Placeholder, String> values;

    public CoreStatementContext() {
        placeholders = new HashMap<>();
        values = new HashMap<>();
        addPlaceholder(SELECTED_RESOURCE);
        addPlaceholder(SELECTED_DEPLOYMENT);
    }

    @Override
    public AddressTemplate.Segment resolve(AddressTemplate.Segment segment) {
        if (segment.containsPlaceholder()) {
            String placeholderName = segment.placeholder();
            Placeholder placeholder = getPlaceholder(placeholderName);
            if (placeholder != null) {
                String resolvedValue = getValue(placeholder);
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
                throw new ResolveException("Unknown placeholder " + placeholderName + " in segment " + segment);
            }
        } else {
            return segment;
        }
    }

    @Override
    public void addPlaceholder(Placeholder placeholder) {
        placeholders.put(placeholder.name, placeholder);
    }

    @Override
    public Placeholder getPlaceholder(String name) {
        return placeholders.get(name);
    }

    @Override
    public void assignValue(String placeholder, String value) {
        Placeholder p = placeholders.get(placeholder);
        if (p != null) {
            values.put(p, value);
        }
    }

    @Override
    public String getValue(Placeholder placeHolder) {
        return values.get(placeHolder);
    }
}
