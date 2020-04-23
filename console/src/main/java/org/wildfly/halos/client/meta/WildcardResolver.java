package org.wildfly.halos.client.meta;

class WildcardResolver implements AddressTemplate.SegmentResolver {

    @Override
    public AddressTemplate.Segment resolve(StatementContext context, AddressTemplate template,
            AddressTemplate.Segment segment, boolean first, boolean last, int index) {
        // use wildcards where possible
        if (segment.containsPlaceholder()) {
            if (segment.hasKey()) {
                return new AddressTemplate.Segment(segment.key, "*");
            } else {
                String placeholderName = segment.placeholder();
                Placeholder placeholder = context.placeholder(placeholderName);
                if (placeholder == null) {
                    throw new ResolveException("Unknown placeholder " + placeholderName + " in " + template);
                }
                return new AddressTemplate.Segment(placeholder.resource, "*");
            }
        } else if (last && !"*".equals(segment.value)) {
            return new AddressTemplate.Segment(segment.key, "*");
        }
        return segment;
    }
}
