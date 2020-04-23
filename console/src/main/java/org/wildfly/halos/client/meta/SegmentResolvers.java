package org.wildfly.halos.client.meta;

import javax.inject.Singleton;

@Singleton
public class SegmentResolvers {

    private final AddressTemplate.SegmentResolver resolver;

    public SegmentResolvers() {
        // we're using only one resolver atm.
        resolver = new WildcardResolver();
    }

    public AddressTemplate.SegmentResolver resourceDescriptionResolver() {
        return resolver;
    }

    public AddressTemplate.SegmentResolver securityContextResolver() {
        return resolver;
    }
}
