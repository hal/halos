/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.halos.console.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import jsinterop.annotations.JsIgnore;
import org.wildfly.halos.console.dmr.ModelNode;
import org.wildfly.halos.console.dmr.ModelNodeHelper;
import org.wildfly.halos.console.dmr.Property;
import org.wildfly.halos.console.dmr.ResourceAddress;

import static java.util.stream.Collectors.joining;

/**
 * Template for a DMR address which might contain multiple variable parts.
 * <p>
 * An address template can be defined using the following BNF:
 * <pre>
 * &lt;address template&gt; ::= "/" | &lt;segment&gt;
 * &lt;segment&gt;          ::= &lt;tuple&gt; | &lt;segment&gt;"/"&lt;tuple&gt;
 * &lt;tuple&gt;            ::= &lt;variable&gt; | &lt;key&gt;"="&lt;value&gt;
 * &lt;variable&gt;         ::= "{"&lt;alpha&gt;"}"
 * &lt;key&gt;              ::= &lt;alpha&gt;
 * &lt;value&gt;            ::= &lt;variable&gt; | &lt;alpha&gt; | "*"
 * &lt;alpha&gt;            ::= &lt;upper&gt; | &lt;lower&gt;
 * &lt;upper&gt;            ::= "A" | "B" | … | "Z"
 * &lt;lower&gt;            ::= "a" | "b" | … | "z"
 * </pre>
 * <p>
 * To get a fully qualified address from an address template use the method <code>resolve()</code>.
 */
public final class AddressTemplate implements Iterable<AddressTemplate.Segment> {

    /** The root template */
    public static final AddressTemplate ROOT = AddressTemplate.of("/");
    public static final String EQUALS = "=";

    // ------------------------------------------------------ factory methods

    /** Creates a new address template from an encoded string template. */
    public static AddressTemplate of(String template) {
        return new AddressTemplate(withSlash(template));
    }

    /** Creates a new address template from a well-known placeholder. */
    // public static AddressTemplate of(Expression placeholder) {
    //     return AddressTemplate.of(String.join("/", placeholder.expression()));
    // }

    /**
     * Creates a new address template from a placeholder and an encoded string template. '/' characters inside values
     * must have been encoded using {@link ModelNodeHelper#encodeValue(String)}.
     */
    // public static AddressTemplate of(Expression placeholder, String template) {
    //     return AddressTemplate.of(String.join("/", placeholder.expression(), withoutSlash(template)));
    // }

    /**
     * Turns a resource address into an address template which is the opposite of {@link #resolve(StatementContext,
     * String...)}.
     */
    @JsIgnore
    public static AddressTemplate of(ResourceAddress address) {
        return of(address, null);
    }

    /**
     * Turns a resource address into an address template which is the opposite of {@link #resolve(StatementContext,
     * String...)}. Use the {@link Unresolver} function to specify how the segments of the resource address are
     * "unresolved". It is called for each segment of the specified resource address.
     */
    @JsIgnore
    public static AddressTemplate of(ResourceAddress address, Unresolver unresolver) {
        int index = 0;
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        if (address.isDefined()) {
            int size = address.size();
            for (Iterator<Property> iterator = address.asPropertyList().iterator(); iterator.hasNext(); ) {
                Property property = iterator.next();
                String name = property.getName();
                String value = property.getValue().asString();
                Segment segment = new Segment(name, value);

                String unresolvedSegment = unresolver == null
                        ? name + EQUALS + value
                        : unresolver.unresolve(segment, first, !iterator.hasNext(), index, size);
                builder.append(unresolvedSegment);

                if (iterator.hasNext()) {
                    builder.append("/");
                }
                first = false;
                index++;
            }
        }
        return of(builder.toString());
    }

    private static String withoutSlash(String template) {
        if (template != null) {
            return template.startsWith("/") ? template.substring(1) : template;
        }
        return null;
    }

    private static String withSlash(String template) {
        if (template != null && !template.startsWith("/")) {
            return "/" + template;
        }
        return template;
    }

    // ------------------------------------------------------ template instance

    private final String template;
    private final LinkedList<Segment> segments;

    /**
     * Creates a new instance from an encoded string template. '/' characters inside values must have been encoded using
     * {@link ModelNodeHelper#encodeValue(String)}
     *
     * @param template the encoded template.
     */
    private AddressTemplate(String template) {
        assert template != null : "template must not be null";
        this.segments = parse(template);
        this.template = join(segments);
    }

    private LinkedList<Segment> parse(String template) {
        LinkedList<Segment> segments = new LinkedList<>();

        if (template.equals("/")) {
            return segments;
        }

        StringTokenizer tok = new StringTokenizer(template);
        while (tok.hasMoreTokens()) {
            String nextToken = tok.nextToken();
            if (nextToken.contains(EQUALS)) {
                String[] split = nextToken.split(EQUALS);
                segments.add(new Segment(split[0], split[1]));
            } else {
                segments.add(new Segment(nextToken));
            }
        }
        return segments;
    }

    private String join(List<Segment> segments) {
        return segments.stream().map(Segment::toString).collect(joining("/"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressTemplate)) {
            return false;
        }

        AddressTemplate that = (AddressTemplate) o;
        return template.equals(that.template);

    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result;
        return result;
    }

    /** @return the string representation of this address template */
    @Override
    public String toString() {
        return template.length() == 0 ? "/" : template;
    }

    // ------------------------------------------------------ append / sub and parent

    /**
     * Appends the specified encoded template to this template and returns a new template. If the specified template
     * does not start with a slash, '/' is automatically appended. '/' characters inside values must have been encoded
     * using {@link ModelNodeHelper#encodeValue(String)}.
     *
     * @param template the encoded template to append (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    public AddressTemplate append(String template) {
        String slashTemplate = template.startsWith("/") ? template : "/" + template;
        return AddressTemplate.of(this.template + slashTemplate);
    }

    public AddressTemplate append(AddressTemplate template) {
        return append(template.toString());
    }

    /**
     * Works like {@link List#subList(int, int)} over the tokens of this template and throws the same exceptions.
     *
     * @param fromIndex low endpoint (inclusive) of the sub template
     * @param toIndex   high endpoint (exclusive) of the sub template
     * @return a new address template containing the specified tokens.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<tt>fromIndex &lt; 0 || toIndex &gt; size
     *                                   || fromIndex &gt; toIndex</tt>)
     */
    public AddressTemplate subTemplate(int fromIndex, int toIndex) {
        LinkedList<Segment> subSegments = new LinkedList<>(this.segments.subList(fromIndex, toIndex));
        return AddressTemplate.of(join(subSegments));
    }

    /** @return the parent address template or the root template */
    public AddressTemplate getParent() {
        if (isEmpty() || size() == 1) {
            return AddressTemplate.of("/");
        } else {
            return subTemplate(0, size() - 1);
        }
    }

    // ------------------------------------------------------ properties

    /** @return the first segment or null if this address template is empty. */
    public Segment first() {
        if (!segments.isEmpty() && segments.getFirst().hasKey()) {
            return segments.getFirst();
        }
        return null;
    }

    /** @return the last segment or null if this address template is empty. */
    public Segment last() {
        if (!segments.isEmpty() && segments.getFirst().hasKey()) {
            return segments.getLast();
        }
        return null;
    }

    /** @return true if this template contains no tokens, false otherwise */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /** @return the number of tokens */
    public int size() {
        return segments.size();
    }

    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    /** @return the address template */
    String getTemplate() {
        return template;
    }


    // ------------------------------------------------------ resolve

    /**
     * Resolve this address template against the specified statement context.
     *
     * @param context   the statement context
     * @param wildcards An optional list of values which are used to resolve any wildcards in this address template from
     *                  left to right
     * @return a fully qualified resource address which might be empty, but which does not contain any variable parts.
     */
    public ResourceAddress resolve(StatementContext context, String... wildcards) {
        if (isEmpty()) {
            return ResourceAddress.root();
        }

        int wildcardCount = 0;
        ModelNode model = new ModelNode();

        for (Segment segment : segments) {
            String key;
            String value;
            // if (segment.hasKey()) {
                key = segment.key;
                if (isVariable(segment.value)) {
                    value = context.resolve(segment);
                } else if ("*".equals(segment.value) && wildcards != null &&
                        wildcards.length > 0 && wildcardCount < wildcards.length) {
                    value = wildcards[wildcardCount];
                    wildcardCount++;
                } else {
                    value = segment.value;
                }
            // } else {
            //     Expression expression = Expression.from(segment.value);
            //     if (expression == null) {
            //         throw new IllegalArgumentException(
            //                 "Invalid or unknown expression '" + segment.value + "' in address template " + this);
            //     }
            //     key = expression.resource();
            //     value = context.resolve(segment);
            // }
            model.add(key, ModelNodeHelper.decodeValue(value));
        }
        return new ResourceAddress(model);
    }

    private boolean isVariable(String value) {
        return value != null && value.startsWith("{") && value.endsWith("}");
    }

    // ------------------------------------------------------ inner classes

    @FunctionalInterface
    public interface Unresolver {

        String unresolve(Segment segment, boolean first, boolean last, int index, int size);
    }

    public static class Segment {

        public final String key;
        public final String value;

        Segment(String key, String value) {
            this.key = key;
            this.value = value;
        }

        Segment(String value) {
            this.key = null;
            this.value = value;
        }

        boolean hasKey() {
            return key != null;
        }

        @Override
        public String toString() {
            return hasKey() ? key + EQUALS + value : value;
        }
    }

    private static class StringTokenizer {

        private final String delim;
        private final String s;
        private final int len;

        private int pos;
        private String next;

        StringTokenizer(String s) {
            this.s = s;
            this.delim = "/";
            len = s.length();
        }

        String nextToken() {
            if (!hasMoreTokens()) {
                throw new NoSuchElementException();
            }
            String result = next;
            next = null;
            return result;
        }

        boolean hasMoreTokens() {
            if (next != null) {
                return true;
            }
            // skip leading delimiters
            while (pos < len && delim.indexOf(s.charAt(pos)) != -1) {
                pos++;
            }

            if (pos >= len) {
                return false;
            }

            int p0 = pos++;
            while (pos < len && delim.indexOf(s.charAt(pos)) == -1) {
                pos++;
            }

            next = s.substring(p0, pos++);
            return true;
        }
    }
}
