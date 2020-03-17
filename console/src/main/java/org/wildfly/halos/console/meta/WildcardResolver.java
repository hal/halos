package org.wildfly.halos.console.meta;

class WildcardResolver implements AddressTemplate.Resolver {

    @Override
    public AddressTemplate.Segment resolve(StatementContext context, AddressTemplate template,
            AddressTemplate.Segment segment, boolean first, boolean last, int index) {
        // use wildcards where possible
        if (segment.containsPlaceholder()) {
            if (segment.hasKey()) {
                return new AddressTemplate.Segment(segment.key, "*");
            } else {
                String placeholderName = segment.placeholder();
                Placeholder placeholder = context.getPlaceholder(placeholderName);
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
