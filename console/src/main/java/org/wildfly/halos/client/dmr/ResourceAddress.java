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
package org.wildfly.halos.client.dmr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a fully qualified DMR address ready to be put into a DMR operation. The address consists of 0-n segments
 * with a name and a value for each segment.
 */
public class ResourceAddress extends ModelNode {

    /** @return the empty (root) address */
    public static ResourceAddress root() {
        // Do not replace this with a static constant! In most cases the returned address is modified somehow.
        return new ResourceAddress();
    }

    /** Creates a new resource address from the specified string. */
    public static ResourceAddress from(String address) {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("Address must not be null or empty");
        }
        String safeAddress = address.startsWith("/") ? address.substring(1) : address;
        ResourceAddress ra = new ResourceAddress();

        String[] segments = safeAddress.split("/");
        for (String segment : segments) {
            if (segment != null && segment.length() != 0) {
                String[] kv = segment.split("=");
                if (kv.length == 2) {
                    ra.add(kv[0], kv[1]);
                }
            }
        }
        return ra;
    }

    public ResourceAddress() {
        setEmptyList();
    }

    public ResourceAddress(ModelNode address) {
        set(address);
    }

    /**
     * Adds the specified segment to this address.
     *
     * @param propertyName  the property name
     * @param propertyValue the property value
     * @return this address with the specified segment added
     */
    public ResourceAddress add(String propertyName, String propertyValue) {
        add().set(propertyName, propertyValue);
        return this;
    }

    /**
     * Adds the specified address to this address.
     *
     * @param address The address to add.
     * @return this address with the specified address added
     */
    public ResourceAddress add(ResourceAddress address) {
        if (address != null) {
            for (Property property : address.asPropertyList()) {
                add(property.getName(), property.getValue().asString());
            }
        }
        return this;
    }

    /** @return the value of the first segment or null if this address is empty. */
    public String firstValue() {
        List<Property> properties = asPropertyList();
        if (!properties.isEmpty()) {
            return properties.get(0).getValue().asString();
        }
        return null;
    }

    /** @return the name of the last segment or null if this address is empty. */
    public String lastName() {
        List<Property> properties = asPropertyList();
        if (!properties.isEmpty()) {
            return properties.get(properties.size() - 1).getName();
        }
        return null;
    }

    /** @return the value of the last segment or null if this address is empty. */
    public String lastValue() {
        List<Property> properties = asPropertyList();
        if (!properties.isEmpty()) {
            return properties.get(properties.size() - 1).getValue().asString();
        }
        return null;
    }

    /** @return the parent address or the root address if this address has no parent. */
    public ResourceAddress getParent() {
        if (this.equals(root()) || asList().isEmpty()) {
            return this;
        }
        List<ModelNode> parent = new ArrayList<>(asList());
        parent.remove(parent.size() - 1);
        return new ResourceAddress(new ModelNode().set(parent));
    }

    /** @return the number of segments. */
    public int size() {
        return isDefined() ? asList().size() : 0;
    }

    /** @return whether this address is empty. */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Replaces the value in the specified segment
     *
     * @param name     The name of the segment.
     * @param newValue The new value.
     * @return this address containing the replaced value
     */
    public ResourceAddress replaceValue(String name, String newValue) {
        ResourceAddress newAddress = new ResourceAddress();
        for (Property property : asPropertyList()) {
            if (name.equals(property.getName())) {
                newAddress.add(name, newValue);
            } else {
                newAddress.add(property.getName(), property.getValue().asString());
            }
        }
        return newAddress;
    }

    /** @return the address as string */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isDefined()) {
            builder.append("/");
            for (Iterator<Property> iterator = asPropertyList().iterator(); iterator.hasNext(); ) {
                Property segment = iterator.next();
                builder.append(segment.getName()).append("=").append(segment.getValue().asString());
                if (iterator.hasNext()) {
                    builder.append("/");
                }
            }
        }
        return builder.toString();
    }
}
