package org.wildfly.halos.console.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.stream.Collectors.joining;

public class MetadataRequest {

    private final Map<AddressTemplate, MetadataRegistry.Scope> request;

    public MetadataRequest() {
        request = new HashMap<>();
    }

    @Override
    public String toString() {
        String value = request.entrySet().stream()
                .map(entry -> entry.getValue().andTemplate(entry.getKey()))
                .collect(joining(", "));
        return "MetadataRequest(" + value + ")";
    }

    public MetadataRequest normal(AddressTemplate template) {
        request.put(template, MetadataRegistry.Scope.NORMAL);
        return this;
    }

    public MetadataRequest recursive(AddressTemplate template) {
        request.put(template, MetadataRegistry.Scope.RECURSIVE);
        return this;
    }

    public MetadataRequest optional(AddressTemplate template) {
        request.put(template, MetadataRegistry.Scope.OPTIONAL);
        return this;
    }

    public MetadataRequest optionalRecursive(AddressTemplate template) {
        request.put(template, MetadataRegistry.Scope.OPTIONAL_RECURSIVE);
        return this;
    }

    public void forEach(BiConsumer<? super AddressTemplate, ? super MetadataRegistry.Scope> action) {
        request.forEach(action);
    }
}
