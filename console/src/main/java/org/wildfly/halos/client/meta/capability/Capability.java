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
package org.wildfly.halos.client.meta.capability;

import java.util.LinkedHashSet;
import java.util.Set;

import org.wildfly.halos.client.meta.AddressTemplate;

public class Capability {

    public final String name;
    private final Set<AddressTemplate> templates;

    public Capability(final String name) {
        this.name = name;
        this.templates = new LinkedHashSet<>();
    }

    @Override
    public String toString() {
        return "Capability(" + name + " -> " + templates + ")";
    }

    public void addTemplate(final AddressTemplate template) {
        templates.add(template);
    }

    public Iterable<AddressTemplate> templates() {
        return templates;
    }
}
