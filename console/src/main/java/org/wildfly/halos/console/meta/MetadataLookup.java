package org.wildfly.halos.console.meta;

import java.util.HashMap;
import java.util.Map;

/** Container used when multiple metadata schould be received with {@link MetadataRegistry#findAll(Iterable)} */
public class MetadataLookup {

    private final Map<AddressTemplate, Metadata> metadata;

    MetadataLookup() {
        metadata = new HashMap<>();
    }

    public Metadata get(AddressTemplate template) {
        return metadata.get(template);
    }
}
