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
package org.wildfly.halos.console.meta.capability;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.wildfly.halos.console.meta.AddressTemplate;

/** Provides access to static fall-back capabilities for servers which don't support a capabilities-registry. */
@Singleton
public class Capabilities {

    private final Map<String, Capability> registry;

    public Capabilities() {
        this.registry = new HashMap<>();
    }

    /** Looks up a capability from the local cache. Returns an empty collection if no such capability was found. */
    public Iterable<AddressTemplate> lookup(String name) {
        if (contains(name)) {
            return registry.get(name).templates();
        }
        return Collections.emptyList();
    }

    public boolean contains(String name) {
        return registry.containsKey(name);
    }

    public void register(String name, AddressTemplate first, AddressTemplate... rest) {
        safeGet(name).addTemplate(first);
        if (rest != null) {
            for (AddressTemplate template : rest) {
                safeGet(name).addTemplate(template);
            }
        }
    }

    public void register(String name, Iterable<AddressTemplate> templates) {
        for (AddressTemplate template : templates) {
            safeGet(name).addTemplate(template);
        }
    }

    public void register(Capability capability) {
        if (contains(capability.name)) {
            Capability existing = registry.get(capability.name);
            for (AddressTemplate template : capability.templates()) {
                existing.addTemplate(template);
            }
        } else {
            registry.put(capability.name, capability);
        }
    }

    private Capability safeGet(String name) {
        if (registry.containsKey(name)) {
            return registry.get(name);
        } else {
            Capability capability = new Capability(name);
            registry.put(name, capability);
            return capability;
        }
    }
}
