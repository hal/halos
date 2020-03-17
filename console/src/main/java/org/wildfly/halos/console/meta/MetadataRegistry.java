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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import elemental2.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.halos.console.dmr.ResourceAddress;

/** Registry for resource {@link Metadata}. */
@Singleton
public class MetadataRegistry {

    private static final int RESOURCE_DESCRIPTION_SIZE = 250;
    private static final String RESOURCE_DESCRIPTION_TYPE = "resource description";
    private static final int SECURITY_CONTEXT_SIZE = 300;
    private static final String SECURITY_CONTEXT_TYPE = "security context";
    private static final AddressTemplate.Resolver WILDCARD_RESOLVER = new WildcardResolver();
    private static final Logger logger = LoggerFactory.getLogger(MetadataRegistry.class);

    private final StatementContext statementContext;
    private final Capabilities capabilities;
    private final Cache<ResourceAddress, ResourceDescription> resourceDescriptions;
    private final Cache<ResourceAddress, SecurityContext> securityContexts;

    @Inject
    public MetadataRegistry(StatementContext statementContext, Capabilities capabilities) {
        this.statementContext = statementContext;
        this.capabilities = capabilities;
        this.resourceDescriptions = CacheBuilder.newBuilder()
                .maximumSize(RESOURCE_DESCRIPTION_SIZE)
                .recordStats()
                .removalListener(notification -> logger.debug("Remove {} from {} cache: {}",
                        notification.getKey(), RESOURCE_DESCRIPTION_TYPE, notification.getCause()))
                .build();
        this.securityContexts = CacheBuilder.newBuilder()
                .maximumSize(SECURITY_CONTEXT_SIZE)
                .recordStats()
                .removalListener(
                        notification -> logger.debug("Remove {} from {} cache: {}",
                                notification.getKey(), SECURITY_CONTEXT_TYPE, notification.getCause()))
                .build();
    }

    public Promise<MetadataLookup> findAll(Iterable<AddressTemplate> templates) {
        Promise<MetadataLookup> promise = new Promise<>((resolve, reject) -> {

        });
        return promise;
    }

    public Promise<Metadata> find(AddressTemplate template) {
        Promise<Metadata> promise = new Promise<>((resolve, reject) -> {
            Metadata metadata = failSafeGet(template);
            if (metadata.description != null && metadata.securityContext != null) {
                resolve.onInvoke(metadata);
            } else {
                // TODO execute r-r-d operations
            }
        });
        return promise;
    }

    public Metadata get(AddressTemplate template) throws MissingMetadataException {
        Metadata metadata = failSafeGet(template);
        if (metadata.description == null) {
            throw new MissingMetadataException(RESOURCE_DESCRIPTION_TYPE, template);
        }
        if (metadata.securityContext == null) {
            throw new MissingMetadataException(SECURITY_CONTEXT_TYPE, template);
        }
        return metadata;
    }

    private Metadata failSafeGet(AddressTemplate template) {
        ResourceAddress address = template.resolve(statementContext, WILDCARD_RESOLVER);
        ResourceDescription resourceDescription = resourceDescriptions.getIfPresent(address);
        SecurityContext securityContext = securityContexts.getIfPresent(address);
        return new Metadata(template, resourceDescription, securityContext, capabilities);
    }
}
