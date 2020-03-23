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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.halos.console.config.Instances;
import org.wildfly.halos.console.dmr.ModelNode;
import org.wildfly.halos.console.meta.capability.Capabilities;
import org.wildfly.halos.console.meta.description.ResourceDescription;
import org.wildfly.halos.console.meta.security.SecurityContext;

import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.HAL_RECURSIVE;
import static org.wildfly.halos.console.meta.AddressTemplate.ROOT;
import static org.wildfly.halos.console.meta.security.SecurityContext.RWX;

/** Simple data struct for common metadata. Used to keep the method signatures small and tidy. */
public class Metadata {

    private static final Logger logger = LoggerFactory.getLogger(Metadata.class);

    public static Metadata empty() {
        return new Metadata(ROOT, new ResourceDescription(new ModelNode()), RWX, new Capabilities(null), false);
    }

    // public static Metadata staticDescription(TextResource description) {
    //     return Metadata.staticDescription(StaticResourceDescription.from(description));
    // }

    /** Constructs a Metadata with read-write-execution permissions, and a non-working capabilities object. */
    public static Metadata staticDescription(ResourceDescription description) {
        return new Metadata(ROOT, new ResourceDescription(description), RWX, new Capabilities(null), false);
    }

    /**
     * Constructs a Metadata with read-write-execution permissions, and a working capabilities object based on the
     * environment object.
     */
    public static Metadata staticDescription(ResourceDescription description, Instances instances) {
        return new Metadata(ROOT, new ResourceDescription(description), RWX, new Capabilities(instances), false);
    }

    public final AddressTemplate template;
    public final ResourceDescription description;
    public final SecurityContext securityContext;
    public final Capabilities capabilities;
    final boolean recursive;

    Metadata(AddressTemplate template, ResourceDescription description, SecurityContext securityContext,
            Capabilities capabilities, boolean recursive) {
        this.template = template;
        this.description = description;
        this.securityContext = securityContext;
        this.capabilities = capabilities;
        this.recursive = recursive;
    }

    boolean nothingPresent() {
        return description == null && securityContext == null;
    }

    boolean allPresent() {
        if (recursive) {
            return description != null && securityContext != null
                    && description.get(HAL_RECURSIVE).asBoolean(false)
                    && securityContext.get(HAL_RECURSIVE).asBoolean(false);
        } else {
            return description != null && securityContext != null;
        }
    }
}
