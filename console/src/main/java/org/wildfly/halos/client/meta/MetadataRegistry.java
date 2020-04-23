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
package org.wildfly.halos.client.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.wildfly.halos.client.dmr.Composite;
import org.wildfly.halos.client.dmr.CompositeResult;
import org.wildfly.halos.client.dmr.Dispatcher;
import org.wildfly.halos.client.dmr.ModelNode;
import org.wildfly.halos.client.dmr.Operation;
import org.wildfly.halos.client.dmr.ResourceAddress;
import org.wildfly.halos.client.meta.capability.Capabilities;
import org.wildfly.halos.client.meta.description.ResourceDescription;
import org.wildfly.halos.client.meta.security.SecurityContext;
import org.wildfly.halos.client.util.Lists;

import static java.util.stream.Collectors.toList;
import static org.wildfly.halos.client.dmr.ModelDescriptionConstants.*;

/** Registry for resource {@link Metadata}. */
@Singleton
public class MetadataRegistry {

    private static final int RRD_DEPTH = 3;
    private static final int BATCH_SIZE = 3;
    // private static final int RESOURCE_DESCRIPTION_SIZE = 250;
    // private static final int SECURITY_CONTEXT_SIZE = 300;
    private static final String DEFAULT_LOCALE = "en";
    private static final CompositeResult OPTIONAL_COMPOSITE_RESULT = new CompositeResult(new Composite(),
            new ModelNode());

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final SegmentResolvers resolvers;
    private final Capabilities capabilities;
    private final Map<ResourceAddress, ResourceDescription> resourceDescriptions;
    private final Map<ResourceAddress, SecurityContext> securityContexts;

    @Inject
    public MetadataRegistry(Dispatcher dispatcher, StatementContext statementContext, SegmentResolvers resolvers,
            Capabilities capabilities) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resolvers = resolvers;
        this.capabilities = capabilities;
        this.resourceDescriptions = new HashMap<>();
        // this.resourceDescriptions = CacheBuilder.newBuilder()
        //         .maximumSize(RESOURCE_DESCRIPTION_SIZE)
        //         .recordStats()
        //         .removalListener(notification -> logger.debug("Remove {} from resource description cache: {}",
        //                 notification.getKey(), notification.getCause()))
        //         .build();
        this.securityContexts = new HashMap<>();
        // this.securityContexts = CacheBuilder.newBuilder()
        //         .maximumSize(SECURITY_CONTEXT_SIZE)
        //         .recordStats()
        //         .removalListener(notification -> logger.debug("Remove {} from security context cache: {}",
        //                 notification.getKey(), notification.getCause()))
        //         .build();
    }

    // ------------------------------------------------------ find all

    @SuppressWarnings("unchecked")
    public Promise<MetadataResult> findAll(MetadataRequest request) {
        MetadataResult result = failSafeGet(request);
        if (allPresent(request, result)) {
            return Promise.resolve(result);

        } else {
            List<Operation> operations = new ArrayList<>();
            List<Operation> optionalOperations = new ArrayList<>();
            request.forEach((template, scope) -> {
                Metadata metadata = result.get(template);
                if (!allPresent(metadata, scope)) {
                    if (scope.optional()) {
                        optionalOperations.addAll(rrdOperations(template, metadata, scope));
                    } else {
                        operations.addAll(rrdOperations(template, metadata, scope));
                    }
                }
            });
            if (operations.isEmpty() && optionalOperations.isEmpty()) {
                throw new MetadataException("Unable to create r-r-d operations for " + request);
            }
            List<Promise<CompositeResult>> promises = new ArrayList<>();
            List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
            piles.stream()
                    .map(Composite::new)
                    .map(dispatcher::execute)
                    .forEach(promises::add);
            optionalOperations.stream()
                    .map(operation -> dispatcher
                            .execute(new Composite(operations))
                            .catch_(error -> Promise.resolve(OPTIONAL_COMPOSITE_RESULT)))
                    .forEach(promises::add);
            // Use object to avoid ClassCastException, since CompositeResult is not a JS type!
            Promise<Object>[] objects = promises.toArray(new Promise[0]);
            return Promise.all(objects).then(results -> {
                CompositeResult[] compositeResults = Js.uncheckedCast(results);
                for (CompositeResult compositeResult : compositeResults) {
                    if (compositeResult != OPTIONAL_COMPOSITE_RESULT) {
                        RrdResult rrdResult = new CompositeRrdParser().parse(compositeResult);
                        resourceDescriptions.putAll(rrdResult.resourceDescriptions);
                        securityContexts.putAll(rrdResult.securityContexts);
                    }
                }
                result.replaceAll((template, metadata) -> get(template)); // use get now instead of failSafeGet
                return Promise.resolve(result);
            });
        }
    }

    // ------------------------------------------------------ find

    public Promise<Metadata> find(AddressTemplate template) {
        return find(template, Scope.NORMAL);
    }

    public Promise<Metadata> find(AddressTemplate template, Scope scope) {
        Metadata metadata = failSafeGet(template);
        if (allPresent(metadata, scope)) {
            return Promise.resolve(metadata);

        } else {
            List<Operation> operations = rrdOperations(template, metadata, scope);
            if (operations.isEmpty()) {
                throw new MetadataException("Unable to create r-r-d operation for " + scope.andTemplate(template));
            } else if (operations.size() == 1) {
                Operation operation = operations.get(0);
                return dispatcher.execute(operation).then(modelNode -> {
                    RrdResult rrdResult = new SingleRrdParser(new RrdResult())
                            .parse(operation.address, modelNode, scope.recursive());
                    resourceDescriptions.putAll(rrdResult.resourceDescriptions);
                    securityContexts.putAll(rrdResult.securityContexts);
                    return Promise.resolve(get(template)); // use get now instead of failSafeGet
                });
            } else {
                return dispatcher.execute(new Composite(operations)).then(compositeResult -> {
                    RrdResult rrdResult = new CompositeRrdParser().parse(compositeResult);
                    resourceDescriptions.putAll(rrdResult.resourceDescriptions);
                    securityContexts.putAll(rrdResult.securityContexts);
                    return Promise.resolve(get(template)); // use get now instead of failSafeGet
                });
            }
        }
    }

    // ------------------------------------------------------ get

    public Metadata get(AddressTemplate template) {
        return get(template, Scope.NORMAL);
    }

    public Metadata get(AddressTemplate template, Scope scope) {
        Metadata metadata = failSafeGet(template);
        if (allPresent(metadata, scope)) {
            if (metadata.description == null) {
                throw new MetadataException("No resource description found for " + scope.andTemplate(template));
            }
            if (metadata.securityContext == null) {
                throw new MetadataException("No security context found for " + scope.andTemplate(template));
            }
        }
        return metadata;
    }

    // ------------------------------------------------------ internals

    private MetadataResult failSafeGet(MetadataRequest request) {
        MetadataResult result = new MetadataResult();
        request.forEach((template, scope) -> result.put(template, failSafeGet(template)));
        return result;
    }

    private Metadata failSafeGet(AddressTemplate template) {
        ResourceAddress rdAddress = template.resolve(statementContext, resolvers.resourceDescriptionResolver());
        ResourceAddress scAddress = template.resolve(statementContext, resolvers.securityContextResolver());
        ResourceDescription resourceDescription = resourceDescriptions.get(rdAddress);
        SecurityContext securityContext = securityContexts.get(scAddress);
        return new Metadata(template, resourceDescription, securityContext, capabilities);
    }

    private boolean allPresent(MetadataRequest request, MetadataResult result) {
        for (Map.Entry<AddressTemplate, Scope> entry : request.entrySet()) {
            AddressTemplate template = entry.getKey();
            Scope scope = entry.getValue();
            Metadata metadata = result.get(template);
            if (!allPresent(metadata, scope)) {
                return false;
            }
        }
        return true;
    }

    private boolean allPresent(Metadata metadata, Scope scope) {
        boolean notNull = metadata.description != null && metadata.securityContext != null;
        if (scope.recursive()) {
            return notNull && metadata.description.recursive && metadata.securityContext.recursive;
        }
        return notNull;
    }

    private List<Operation> rrdOperations(AddressTemplate template, Metadata metadata, Scope scope) {
        List<Operation.Builder> builders = new ArrayList<>();
        if (metadata.description == null && metadata.securityContext == null) {
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
                    if (scope.recursive()) {
                        builder.param(RECURSIVE_DEPTH, RRD_DEPTH);
                    }
                    builder.param(LOCALE, DEFAULT_LOCALE);
                    return builder.build();
                })
                .collect(toList());
    }

    // ------------------------------------------------------ inner classes

    public enum Scope {
        NORMAL(""),
        RECURSIVE("recursive:/"),
        OPTIONAL("opt:/"),
        OPTIONAL_RECURSIVE("opt+recursive:/");

        private final String prefix;

        Scope(String prefix) {
            this.prefix = prefix;
        }

        boolean recursive() {
            return this == RECURSIVE || this == OPTIONAL_RECURSIVE;
        }

        boolean optional() {
            return this == OPTIONAL || this == OPTIONAL_RECURSIVE;
        }

        String andTemplate(AddressTemplate template) {
            return prefix + template;
        }
    }
}
