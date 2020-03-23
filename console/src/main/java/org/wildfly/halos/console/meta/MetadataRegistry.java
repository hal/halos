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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import elemental2.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.halos.console.dispatch.Dispatcher;
import org.wildfly.halos.console.dmr.Composite;
import org.wildfly.halos.console.dmr.CompositeResult;
import org.wildfly.halos.console.dmr.Operation;
import org.wildfly.halos.console.dmr.ResourceAddress;
import org.wildfly.halos.console.meta.capability.Capabilities;
import org.wildfly.halos.console.meta.description.ResourceDescription;
import org.wildfly.halos.console.meta.security.SecurityContext;

import static java.util.stream.Collectors.toList;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.*;

/** Registry for resource {@link Metadata}. */
@Singleton
public class MetadataRegistry {

    private static final int RRD_DEPTH = 3;
    private static final int BATCH_SIZE = 3;
    private static final int RESOURCE_DESCRIPTION_SIZE = 250;
    private static final int SECURITY_CONTEXT_SIZE = 300;
    private static final String DEFAULT_LOCALE = "en";
    private static final Logger logger = LoggerFactory.getLogger(MetadataRegistry.class);

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final SegmentResolvers resolvers;
    private final Capabilities capabilities;
    private final Cache<ResourceAddress, ResourceDescription> resourceDescriptions;
    private final Cache<ResourceAddress, SecurityContext> securityContexts;

    @Inject
    public MetadataRegistry(Dispatcher dispatcher, StatementContext statementContext, SegmentResolvers resolvers,
            Capabilities capabilities) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resolvers = resolvers;
        this.capabilities = capabilities;
        this.resourceDescriptions = CacheBuilder.newBuilder()
                .maximumSize(RESOURCE_DESCRIPTION_SIZE)
                .recordStats()
                .removalListener(notification -> logger.debug("Remove {} from resource description cache: {}",
                        notification.getKey(), notification.getCause()))
                .build();
        this.securityContexts = CacheBuilder.newBuilder()
                .maximumSize(SECURITY_CONTEXT_SIZE)
                .recordStats()
                .removalListener(notification -> logger.debug("Remove {} from security context cache: {}",
                        notification.getKey(), notification.getCause()))
                .build();
    }

    // ------------------------------------------------------ find all

    public Promise<Map<AddressTemplate, Metadata>> findAll(Iterable<AddressTemplate> templates) {
        return findAll(templates, false);
    }

    // TODO Use metadata request to set scope ((none-)recursive, optional) for each template independently.
    public Promise<Map<AddressTemplate, Metadata>> findAll(Iterable<AddressTemplate> templates, boolean recursive) {
        Map<AddressTemplate, Metadata> allMetadata = failSafeGet(templates, recursive);
        if (allMetadata.values().stream().allMatch(Metadata::allPresent)) {
            return Promise.resolve(allMetadata);

        } else {
            List<Operation> operations = new ArrayList<>();
            allMetadata.forEach((template, metadata) -> operations.addAll(rrdOperations(template, metadata)));
            if (operations.isEmpty()) {
                throw new MetadataException("Unable to create r-r-d operation for " + templates);
            }
            List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
            Promise<CompositeResult>[] promises = piles.stream()
                    .map(Composite::new)
                    .map(dispatcher::execute)
                    .toArray((IntFunction<Promise<CompositeResult>[]>) Promise[]::new);
            return Promise.all(promises).then(results -> {
                for (CompositeResult compositeResult : results) {
                    RrdResult rrdResult = new CompositeRrdParser(recursive).parse(compositeResult);
                    resourceDescriptions.putAll(rrdResult.resourceDescriptions);
                    securityContexts.putAll(rrdResult.securityContexts);
                }
                allMetadata.replaceAll((template, metadata) -> get(template)); // use get now instead of failSafeGet
                return Promise.resolve(allMetadata);
            });
        }
    }

    // ------------------------------------------------------ find

    public Promise<Metadata> find(AddressTemplate template) {
        return find(template, false);
    }

    public Promise<Metadata> find(AddressTemplate template, boolean recursive) {
        Metadata metadata = failSafeGet(template, recursive);
        if (metadata.allPresent()) {
            return Promise.resolve(metadata);

        } else {
            List<Operation> operations = rrdOperations(template, metadata);
            if (operations.isEmpty()) {
                throw new MetadataException("Unable to create r-r-d operation for " + template);
            } else if (operations.size() == 1) {
                Operation operation = operations.get(0);
                return dispatcher.execute(operation).then(modelNode -> {
                    RrdResult rrdResult = new SingleRrdParser(new RrdResult(), recursive)
                            .parse(operation.address,modelNode);
                    resourceDescriptions.putAll(rrdResult.resourceDescriptions);
                    securityContexts.putAll(rrdResult.securityContexts);
                    return Promise.resolve(get(template)); // use get now instead of failSafeGet
                });
            } else {
                return dispatcher.execute(new Composite(operations)).then(compositeResult -> {
                    RrdResult rrdResult = new CompositeRrdParser(recursive).parse(compositeResult);
                    resourceDescriptions.putAll(rrdResult.resourceDescriptions);
                    securityContexts.putAll(rrdResult.securityContexts);
                    return Promise.resolve(get(template)); // use get now instead of failSafeGet
                });
            }
        }
    }

    // ------------------------------------------------------ get

    public Metadata get(AddressTemplate template) {
        return get(template, false);
    }

    public Metadata get(AddressTemplate template, boolean recursive) {
        Metadata metadata = failSafeGet(template, recursive);
        if (metadata.description == null) {
            throw new MetadataException("No resource description found for " + template);
        }
        if (metadata.securityContext == null) {
            throw new MetadataException("No security context found for " + template);
        }
        return metadata;
    }

    // ------------------------------------------------------ internals

    private Map<AddressTemplate, Metadata> failSafeGet(Iterable<AddressTemplate> templates, boolean recursive) {
        Map<AddressTemplate, Metadata> metadata = new HashMap<>();
        for (AddressTemplate template : templates) {
            metadata.put(template, failSafeGet(template, recursive));
        }
        return metadata;
    }

    private Metadata failSafeGet(AddressTemplate template, boolean recursive) {
        ResourceAddress rdAddress = template.resolve(statementContext, resolvers.resourceDescriptionResolver());
        ResourceAddress scAddress = template.resolve(statementContext, resolvers.securityContextResolver());
        ResourceDescription resourceDescription = resourceDescriptions.getIfPresent(rdAddress);
        SecurityContext securityContext = securityContexts.getIfPresent(scAddress);
        return new Metadata(template, resourceDescription, securityContext, capabilities, recursive);
    }

    private List<Operation> rrdOperations(AddressTemplate template, Metadata metadata) {
        List<Operation.Builder> builders = new ArrayList<>();
        if (metadata.nothingPresent()) {
            ResourceAddress rdAddress = template.resolve(statementContext, resolvers.resourceDescriptionResolver());
            ResourceAddress scAddress = template.resolve(statementContext, resolvers.securityContextResolver());
            if (rdAddress.equals(scAddress)) {
                builders.add(new Operation.Builder(rdAddress, READ_RESOURCE_DESCRIPTION_OPERATION)
                        .param(ACCESS_CONTROL, COMBINED_DESCRIPTIONS)
                        .param(OPERATIONS, true));
            } else {
                builders.add(new Operation.Builder(rdAddress, READ_RESOURCE_DESCRIPTION_OPERATION)
                        .param(OPERATIONS, true));
                builders.add(new Operation.Builder(scAddress, READ_RESOURCE_DESCRIPTION_OPERATION)
                        .param(ACCESS_CONTROL, TRIM_DESCRIPTIONS)
                        .param(OPERATIONS, true));
            }

        } else if (metadata.description == null) {
            ResourceAddress address = template.resolve(statementContext,
                    resolvers.resourceDescriptionResolver());
            builders.add(new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                    .param(OPERATIONS, true));

        } else if (metadata.securityContext == null) {
            ResourceAddress address = template.resolve(statementContext, resolvers.securityContextResolver());
            builders.add(new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                    .param(ACCESS_CONTROL, TRIM_DESCRIPTIONS)
                    .param(OPERATIONS, true));
        }

        return builders.stream()
                .map(builder -> {
                    if (metadata.recursive) {
                        builder.param(RECURSIVE_DEPTH, RRD_DEPTH);
                    }
                    builder.param(LOCALE, DEFAULT_LOCALE);
                    return builder.build();
                })
                .collect(toList());
    }
}
