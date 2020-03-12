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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.wildfly.halos.console.config.Instance;

/** Provides access to static fall-back capabilities for servers which don't support a capabilities-registry. */
public class Capabilities {

    private final Instance instance;
    private final Map<String, Capability> registry;

    @Inject
    public Capabilities(Instance instance) {
        this.instance = instance;
        this.registry = new HashMap<>();
    }

    public boolean supportsSuggestions() {
        return instance != null && ManagementModel.supportsCapabilitiesRegistry(instance.managementVersion);
    }

    /**
     * Looks up a capability from the local cache. Returns an empty collection if no such capability was found.
     */
    public Iterable<AddressTemplate> lookup(final String name) {
        if (contains(name)) {
            return registry.get(name).getTemplates();
        }
        return Collections.emptyList();
    }

    public boolean contains(final String name) {
        return registry.containsKey(name);
    }

    public void register(final String name, final AddressTemplate first, final AddressTemplate... rest) {
        safeGet(name).addTemplate(first);
        if (rest != null) {
            for (AddressTemplate template : rest) {
                safeGet(name).addTemplate(template);
            }
        }
    }

    public void register(final String name, final Iterable<AddressTemplate> templates) {
        for (AddressTemplate template : templates) {
            safeGet(name).addTemplate(template);
        }
    }

    public void register(final Capability capability) {
        if (contains(capability.getName())) {
            Capability existing = registry.get(capability.getName());
            for (AddressTemplate template : capability.getTemplates()) {
                existing.addTemplate(template);
            }
        } else {
            registry.put(capability.getName(), capability);
        }
    }

    private Capability safeGet(final String name) {
        if (registry.containsKey(name)) {
            return registry.get(name);
        } else {
            Capability capability = new Capability(name);
            registry.put(name, capability);
            return capability;
        }
    }
}
