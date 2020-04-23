package org.wildfly.halos.client.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class MetadataResult {

    private final Map<AddressTemplate, Metadata> result;

    public MetadataResult() {
        result = new HashMap<>();
    }

    public Metadata get(AddressTemplate template) {
        return result.getOrDefault(template, Metadata.empty());
    }

    public void forEach(BiConsumer<? super AddressTemplate, ? super Metadata> action) {
        result.forEach(action);
    }

    public Set<Map.Entry<AddressTemplate, Metadata>> entrySet() {return result.entrySet();}

    Metadata put(AddressTemplate template, Metadata metadata) {
        return result.put(template, metadata);
    }

    void replaceAll(BiFunction<? super AddressTemplate, ? super Metadata, ? extends Metadata> function) {
        result.replaceAll(function);
    }
}
