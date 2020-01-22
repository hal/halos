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
package org.jboss.hal.meta.processing;

import java.util.Map;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

class UpdateRegistryTask implements Task<LookupContext> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateRegistryTask.class);

    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;

    UpdateRegistryTask(ResourceDescriptionRegistry resourceDescriptionRegistry,
            SecurityContextRegistry securityContextRegistry) {
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.securityContextRegistry = securityContextRegistry;
    }

    @Override
    public Completable call(LookupContext context) {
        if (context.updateRegistry()) {
            for (Map.Entry<ResourceAddress, ResourceDescription> entry : context.toResourceDescriptionRegistry.entrySet()) {
                ResourceAddress address = entry.getKey();
                ResourceDescription resourceDescription = entry.getValue();
                resourceDescriptionRegistry.add(address, resourceDescription, context.recursive);
            }
            for (Map.Entry<ResourceAddress, SecurityContext> entry : context.toSecurityContextRegistry.entrySet()) {
                ResourceAddress address = entry.getKey();
                SecurityContext securityContext = entry.getValue();
                securityContextRegistry.add(address, securityContext, context.recursive);
            }
            logger.debug("Added {} resource descriptions and {} security contexts to the registries",
                    context.toResourceDescriptionRegistry.size(), context.toSecurityContextRegistry.size());
        }
        return Completable.complete();
    }
}
